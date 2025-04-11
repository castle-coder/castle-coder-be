package com.castle.runpod.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

public class RunPodApiClient {

    private final WebClient webClient;
    private final String endpointId; // 고정된 endpoint id

    public RunPodApiClient(String apiKey, String endpointId) {
        this.endpointId = endpointId;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.runpod.ai")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // Job 제출
    public Mono<Map> submitJob(Map payload) {
        String runUrl = String.format("/v2/%s/run", endpointId);
        return webClient.post()
                .uri(runUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class);
    }

    // 스트리밍 폴링
    public Flux<Map> pollStream(String jobId) {
        String streamUrl = String.format("/v2/%s/stream/%s", endpointId, jobId);
        return Flux.interval(Duration.ofMillis(300))
                .flatMap(tick -> webClient.get()
                        .uri(streamUrl)
                        .retrieve()
                        .bodyToMono(Map.class))
                .takeUntil(map -> "COMPLETED".equals(map.get("status")));
    }

    // Job 상태 확인
    public Mono<Map> checkJobStatus(String jobId) {
        String url = String.format("/v2/%s/status/%s", endpointId, jobId);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class);
    }
}
