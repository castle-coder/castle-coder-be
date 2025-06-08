package com.castle.runpod.service;

import com.castle.code.error.RunPodErrorCode;
import com.castle.domain.chat.service.ChatService;
import com.castle.exception.RunPodException;
import com.castle.openai.service.OpenAiService;
import com.castle.redis.service.RedisDataService;
import com.castle.runpod.grpc.RunPodGrpcClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunPodAsyncExecutor {

    private final RunPodGrpcClient runPodGrpcClient;
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final RedisDataService redisDataService;
    private final OpenAiService openAiService;

    /* ─────── Micrometer meters ─────── */
    private static final Timer STREAM_TIMER = Timer.builder("runpod.stream.duration")
            .description("Overall time from gRPC start to SSE completion")
            .publishPercentileHistogram()
            .maximumExpectedValue(Duration.ofMinutes(5))
            .register(Metrics.globalRegistry);

    private static final Timer SSE_FLUSH_TIMER = Timer.builder("runpod.sse.flush")
            .description("Time spent in SseEmitter.send(...)")
            .publishPercentileHistogram()
            .maximumExpectedValue(Duration.ofSeconds(1))
            .register(Metrics.globalRegistry);

    private static final Counter TOKENS_COUNTER = Counter.builder("runpod.tokens.total")
            .description("Total number of tokens sent via SSE")
            .register(Metrics.globalRegistry);

    private static final AtomicInteger ACTIVE_STREAMS = new AtomicInteger(0);

    static {
        Gauge.builder("runpod.stream.active", ACTIVE_STREAMS, AtomicInteger::get)
                .description("Current active RunPod streams")
                .register(Metrics.globalRegistry);
    }
    /* ──────────────────────────────── */

    @Async
    public void streamAsync(String prompt,
                            Long chatSessionId,
                            List<String> imageUrls,
                            SseEmitter emitter) {
        ACTIVE_STREAMS.incrementAndGet();
        Timer.Sample sample = Timer.start();

        AtomicBoolean isCompleted = new AtomicBoolean(false);
        try {
            StringBuilder responseBuilder = new StringBuilder();

            String grpcTarget = runPodGrpcClient.runAndGetGrpcTarget(chatSessionId, prompt);
            log.info("RunPod gRPC target: {}", grpcTarget);

            if (grpcTarget == null) {
                emitter.completeWithError(new RunPodException(RunPodErrorCode.RUNPOD_CONNECTION_FAILED));
                return;
            }

            runPodGrpcClient.streamFromGrpc(
                    grpcTarget,
                    prompt,
                    /* onToken */ token -> {
                        try {
                            responseBuilder.append(token);
                            TOKENS_COUNTER.increment();
                            if (!isCompleted.get()) {
                                String json = objectMapper.writeValueAsString(
                                        Map.of("type", "token", "content", token));
                                SSE_FLUSH_TIMER.record(() -> {
                                    try {
                                        emitter.send(
                                                SseEmitter.event().name("message").data(json));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                log.debug("Token sent: {}", token);
                            }
                        } catch (IllegalStateException ignore) {
                            isCompleted.set(true);
                        } catch (IOException e) {
                            isCompleted.set(true);
                            emitter.completeWithError(e);
                        }
                    },
                    /* onComplete */ () -> {
                        if (isCompleted.compareAndSet(false, true)) {
                            SSE_FLUSH_TIMER.record(() -> {
                                try {
                                    emitter.send(
                                            SseEmitter.event()
                                                    .name("message")
                                                    .data(objectMapper.writeValueAsString(
                                                            Map.of("type", "end",
                                                                    "message", "[생성 완료]"))));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            emitter.complete();
                            chatService.saveChatLog(chatSessionId, prompt,
                                    responseBuilder.toString(), imageUrls);
                            openAiService.generateTitle(chatSessionId, prompt);
                        }
                    },
                    /* onError */ error -> {
                        if (isCompleted.compareAndSet(false, true)) {
                            RunPodErrorCode errorCode = (error instanceof TimeoutException) ?
                                    RunPodErrorCode.RUNPOD_API_TIMEOUT :
                                    (error instanceof ConnectException) ?
                                            RunPodErrorCode.RUNPOD_CONNECTION_FAILED :
                                            RunPodErrorCode.RUNPOD_API_INTERNAL_ERROR;

                            SSE_FLUSH_TIMER.record(() -> {
                                try {
                                    emitter.send(
                                            SseEmitter.event()
                                                    .name("message")
                                                    .data(objectMapper.writeValueAsString(
                                                            Map.of("type", "error",
                                                                    "message", errorCode.getErrorResponse()))));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            emitter.completeWithError(error);
                        }
                    }
            );

        } catch (Exception ex) {
            log.error("Exception during async stream processing", ex);
            if (!isCompleted.get()) {
                emitter.completeWithError(ex);
            }
        } finally {
            redisDataService.deleteSingleData("runpod:requestId:" + chatSessionId);
            SecurityContextHolder.clearContext();

            ACTIVE_STREAMS.decrementAndGet();
            sample.stop(STREAM_TIMER);
        }
    }
}
