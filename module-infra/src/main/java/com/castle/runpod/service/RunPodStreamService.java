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

        Long sessionMemberId = chatSession.getMember().getId(); // Lazy 프록시 초기화
        if (!Objects.equals(sessionMemberId, memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }

        SseEmitter emitter = new SseEmitter(0L);
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        StringBuilder responseBuilder = new StringBuilder();
        SecurityContext context = SecurityContextHolder.getContext();

        // chatSession과 member는 스레드에 넘기지 말고 필요한 id만 따로 보관
        Long finalChatSessionId = chatSessionId;

        new Thread(() -> {
            SecurityContextHolder.setContext(context);
            try {
                String grpcTarget = runPodGrpcClient.runAndGetGrpcTarget(prompt);
                if (grpcTarget == null) {
                    emitter.completeWithError(new RunPodException(RunPodErrorCode.RUNPOD_CONNECTION_FAILED));
                    return;
                }

                runPodGrpcClient.streamFromGrpc(
                        grpcTarget,
                        prompt,
                        token -> {
                            responseBuilder.append(token);
                            try {
                                if (!isCompleted.get()) {
                                    emitter.send(SseEmitter.event().name("message").data(token));
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
                                    // 무시
                                }

                                // 여기서 다시 chatSession과 member 조회 (새 트랜잭션)
                                saveChatLog(finalChatSessionId, memberId, prompt, responseBuilder.toString());
                                emitter.complete();
                                System.out.println(1);
                            }
                        },
                        error -> {
                            if (isCompleted.compareAndSet(false, true)) {
                                RunPodErrorCode errorCode = (error instanceof TimeoutException) ?
                                        RunPodErrorCode.RUNPOD_API_TIMEOUT :
                                        (error instanceof ConnectException) ?
                                                RunPodErrorCode.RUNPOD_CONNECTION_FAILED :
                                                RunPodErrorCode.RUNPOD_API_INTERNAL_ERROR;
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("error")
                                            .data(errorCode.getErrorResponse()));
                                } catch (IOException ignore) {
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

    @Transactional
    protected void saveChatLog(Long chatSessionId, Long memberId, String prompt, String response) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

        chatLogRepository.save(ChatLog.builder()
                .chatSession(chatSession)
                .member(chatSession.getMember()) // 같은 트랜잭션이므로 안전
                .prompt(prompt)
                .response(response)
                .build());
    }

}