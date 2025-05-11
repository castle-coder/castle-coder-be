package com.castle.runpod.service;

import com.castle.code.error.ChatErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.code.error.RunPodErrorCode;
import com.castle.domain.chat.entity.ChatLog;
import com.castle.domain.chat.entity.ChatSession;
import com.castle.domain.chat.repository.ChatLogRepository;
import com.castle.domain.chat.repository.ChatSessionRepository;
import com.castle.exception.ChatException;
import com.castle.exception.MemberException;
import com.castle.exception.RunPodException;
import com.castle.runpod.grpc.RunPodGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class RunPodStreamService {

    private final RunPodGrpcClient runPodGrpcClient;
    private final ChatLogRepository chatLogRepository;
    private final ChatSessionRepository chatSessionRepository;

    /**
     * 프론트엔드에 SSE를 통해 스트리밍 전송한다.
     */
    @Transactional
    public SseEmitter streamToClient(String prompt, Long memberId, Long chatSessionId) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

        if (!Objects.equals(chatSession.getMember().getId(), memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }

        SseEmitter emitter = new SseEmitter(0L);
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        StringBuilder responseBuilder = new StringBuilder(); // 응답 텍스트를 누적
        SecurityContext context = SecurityContextHolder.getContext();

        // 별도 스레드에서 RunPod 호출 및 스트리밍 실행
        new Thread(() -> {
            SecurityContextHolder.setContext(context);
            try {
                String grpcTarget = runPodGrpcClient.runAndGetGrpcTarget(prompt);
                if (grpcTarget == null) {
                    emitter.completeWithError(
                            new RunPodException(RunPodErrorCode.RUNPOD_CONNECTION_FAILED)
                    );
                    return;
                }
                runPodGrpcClient.streamFromGrpc(
                        grpcTarget,
                        prompt,
                        token -> {
                            responseBuilder.append(token); // 응답 누적
                            try {
                                if (!isCompleted.get()) {
                                    emitter.send(SseEmitter.event().name("message").data(token));
                                    responseBuilder.append(token);
                                }
                            } catch (IllegalStateException ignore) {
                                isCompleted.set(true);
                            } catch (IOException e) {
                                isCompleted.set(true);
                                emitter.completeWithError(e);
                            }
                        },
                        () -> {
                            if (isCompleted.compareAndSet(false, true)) {

                                try {
                                    emitter.send(SseEmitter.event().name("end").data("[생성 완료]"));
                                } catch (IOException e) {
                                    // 전송 실패는 무시하고 완료
                                }

                                chatLogRepository.save(ChatLog.builder()
                                        .chatSession(chatSession)
                                        .member(chatSession.getMember())
                                        .prompt(prompt)
                                        .response(responseBuilder.toString())
                                        .build());

                                emitter.complete();
                            }
                        },
                        error -> {
                            if (isCompleted.compareAndSet(false, true)) {
                                // RunPodErrorCode 매핑 예시
                                RunPodErrorCode errorCode;

                                if (error instanceof TimeoutException) {
                                    errorCode = RunPodErrorCode.RUNPOD_API_TIMEOUT;
                                } else if (error instanceof ConnectException) {
                                    errorCode = RunPodErrorCode.RUNPOD_CONNECTION_FAILED;
                                } else {
                                    errorCode = RunPodErrorCode.RUNPOD_API_INTERNAL_ERROR;
                                }

                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("error")
                                            .data(errorCode.getErrorResponse())); // JSON으로 직렬화 필요 시 변환
                                } catch (IOException ioException) {
                                    // 전송 실패 시 그냥 종료
                                }

                                emitter.completeWithError(error);
                            }
                        }
                );
            } catch (Exception ex) {
                if (isCompleted.compareAndSet(false, true)) {
                    emitter.completeWithError(ex);
                    }
            } finally {
                SecurityContextHolder.clearContext();
            }
        }).start();

        return emitter;
    }
}
