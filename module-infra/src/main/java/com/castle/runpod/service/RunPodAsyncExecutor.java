package com.castle.runpod.service;

import com.castle.code.error.RunPodErrorCode;
import com.castle.domain.chat.service.ChatService;
import com.castle.exception.RunPodException;
import com.castle.redis.service.RedisDataService;
import com.castle.runpod.grpc.RunPodGrpcClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final RedisDataService redisDataService;

    @Async
    public void streamAsync(String prompt,
                            Long chatSessionId,
                            List<String> imageUrls,
                            SseEmitter emitter) {
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        try {
            StringBuilder responseBuilder = new StringBuilder();

            String grpcTarget = runPodGrpcClient.runAndGetGrpcTarget(chatSessionId, prompt);
            if (grpcTarget == null) {
                emitter.completeWithError(new RunPodException(RunPodErrorCode.RUNPOD_CONNECTION_FAILED));
                return;
            }

            runPodGrpcClient.streamFromGrpc(
                    grpcTarget,
                    prompt,
                    token -> {
                        try {
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
                            if (isCompleted.compareAndSet(false, true)) {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(objectMapper.writeValueAsString(
                                                Map.of("type", "end", "message", "[생성 완료]")
                                        )));
                                emitter.complete();
                                chatService.saveChatLog(chatSessionId, prompt, responseBuilder.toString(), imageUrls);
                            }
                        } catch (IOException e) {
                            log.warn("Failed to send end event.", e);
                        }
                    },
                    error -> {
                        try {
                            // 에러 처리 시 SecurityContext 재설정
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
                    }
            );
        } catch (Exception ex) {
            log.error("비동기 스트림 처리 중 예외 발생", ex);
            if (!isCompleted.get()) {
                emitter.completeWithError(ex);
            }
        } finally {
            // 작업 완료 후 Redis 데이터 삭제(성공 실패 여부 상관 x)
            redisDataService.deleteSingleData("runpod:requestId:" + chatSessionId);
            // 작업 완료 후 SecurityContext 정리
            SecurityContextHolder.clearContext();
        }
    }
}
