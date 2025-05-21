package com.castle.runpod.service;

import com.castle.code.error.ChatErrorCode;
import com.castle.code.error.RunPodErrorCode;
import com.castle.domain.chat.entity.ChatImage;
import com.castle.domain.chat.entity.ChatLog;
import com.castle.domain.chat.entity.ChatSession;
import com.castle.domain.chat.repository.ChatImageRepository;
import com.castle.domain.chat.repository.ChatLogRepository;
import com.castle.domain.chat.repository.ChatSessionRepository;
import com.castle.exception.ChatException;
import com.castle.exception.RunPodException;
import com.castle.runpod.grpc.RunPodGrpcClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunPodAsyncExecutor {

    private final RunPodGrpcClient runPodGrpcClient;
    private final ChatLogRepository chatLogRepository;
    private final ChatImageRepository chatImageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void streamAsync(String prompt,
                            Long memberId,
                            Long chatSessionId,
                            List<String> imageUrls,
                            SseEmitter emitter,
                            SecurityContext context) {
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        try {
            // 비동기 작업 시작 시 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            
            StringBuilder responseBuilder = new StringBuilder();

            String grpcTarget = runPodGrpcClient.runAndGetGrpcTarget(prompt);
            if (grpcTarget == null) {
                emitter.completeWithError(new RunPodException(RunPodErrorCode.RUNPOD_CONNECTION_FAILED));
                return;
            }

            runPodGrpcClient.streamFromGrpc(
                    grpcTarget,
                    prompt,
                    token -> {
                        try {
                            // 각 토큰 처리 시 SecurityContext 재설정
                            SecurityContextHolder.setContext(context);
                            responseBuilder.append(token);
                            if (!isCompleted.get()) {
                                String json = objectMapper.writeValueAsString(Map.of("type", "token", "content", token));
                                emitter.send(SseEmitter.event().name("message").data(json));
                                log.debug("Token sent: {}", token);
                            }
                        } catch (IllegalStateException ignore) {
                            isCompleted.set(true);
                        } catch (IOException e) {
                            isCompleted.set(true);
                            emitter.completeWithError(e);
                        }
                    },
                    () -> {
                        try {
                            // 완료 처리 시 SecurityContext 재설정
                            SecurityContextHolder.setContext(context);
                            if (isCompleted.compareAndSet(false, true)) {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(objectMapper.writeValueAsString(
                                                Map.of("type", "end", "message", "[생성 완료]")
                                        )));
                                emitter.complete();
                                saveChatLog(chatSessionId, memberId, prompt, responseBuilder.toString(), imageUrls);
                            }
                        } catch (IOException e) {
                            log.warn("Failed to send end event.", e);
                        }
                    },
                    error -> {
                        try {
                            // 에러 처리 시 SecurityContext 재설정
                            SecurityContextHolder.setContext(context);
                            if (isCompleted.compareAndSet(false, true)) {
                                RunPodErrorCode errorCode = (error instanceof TimeoutException) ?
                                        RunPodErrorCode.RUNPOD_API_TIMEOUT :
                                        (error instanceof ConnectException) ?
                                                RunPodErrorCode.RUNPOD_CONNECTION_FAILED :
                                                RunPodErrorCode.RUNPOD_API_INTERNAL_ERROR;
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(objectMapper.writeValueAsString(
                                                Map.of("type", "error", "message", errorCode.getErrorResponse())
                                        )));
                                emitter.completeWithError(error);
                            }
                        } catch (IOException ignore) {
                            log.warn("Failed to send error event.", ignore);
                        }
                    },
                    context
            );
        } catch (Exception ex) {
            log.error("비동기 스트림 처리 중 예외 발생", ex);
            if (!isCompleted.get()) {
                emitter.completeWithError(ex);
            }
        } finally {
            // 작업 완료 후 SecurityContext 정리
            SecurityContextHolder.clearContext();
        }
    }

    @Transactional
    protected void saveChatLog(Long chatSessionId,
                               Long memberId,
                               String prompt,
                               String response,
                               List<String> imageUrls) {
        try {
            ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                    .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

            ChatLog chatLog = ChatLog.builder()
                    .chatSession(chatSession)
                    .member(chatSession.getMember())
                    .prompt(prompt)
                    .response(response)
                    .build();

            chatLogRepository.save(chatLog);

            if (imageUrls != null && !imageUrls.isEmpty()) {
                imageUrls.forEach(url -> chatImageRepository.save(
                        ChatImage.builder()
                                .chatLog(chatLog)
                                .imageUrl(url)
                                .build()
                ));
            }
        } catch (Exception e) {
            log.error("채팅 로그 저장 중 오류 발생", e);
            throw e;
        }
    }
}
