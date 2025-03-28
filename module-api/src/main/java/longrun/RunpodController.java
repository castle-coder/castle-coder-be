package longrun;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/runpod")
public class RunpodController {

    private final WebClient webClient;

    public RunpodController(@Value("${runpod.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.runpod.ai")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> submitJobAndStreamOutput(@RequestBody RunpodRequest request) {
        String endpointId = request.getServerEndpointId();
        String prompt = request.getPrompt();

        // job 제출을 위한 요청 바디 구성
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("prompt", prompt);
        inputMap.put("stream", true);

        Map<String, Object> samplingParams = new HashMap<>();
        samplingParams.put("max_tokens", 1000);
        inputMap.put("sampling_params", samplingParams);

        Map<String, Object> payload = new HashMap<>();
        payload.put("input", inputMap);

        String runUrl = String.format("/v2/%s/run", endpointId);

        // 1. job 제출 → job ID 획득
        Mono<Map> jobResponseMono = webClient.post()
                .uri(runUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class);

        return jobResponseMono.flatMapMany(response -> {
            Object idObj = response.get("id");
            if (idObj == null) {
                return Flux.error(new RuntimeException("Failed to get a job ID from the response."));
            }
            String jobId = idObj.toString();
            System.out.println("Job ID: " + jobId);

            // 2. 스트리밍 URL 구성
            String streamUrl = String.format("/v2/%s/stream/%s", endpointId, jobId);

            StringBuilder fullText = new StringBuilder();
            // 3. 1초 간격으로 스트림 엔드포인트를 폴링하여 응답 처리
            return Flux.interval(Duration.ofMillis(300))
                    .flatMap(tick -> webClient.get()
                            .uri(streamUrl)
                            .retrieve()
                            .bodyToMono(Map.class))
                    .takeUntil(map -> "COMPLETED".equals(map.get("status")))
                    .flatMap(map -> {
                        if (map.containsKey("stream")) {
                            List<Map<String, Object>> streamList = (List<Map<String, Object>>) map.get("stream");
                            return Flux.fromIterable(streamList)
                                .map(item -> {
                                    Map<String, Object> output = (Map<String, Object>) item.get("output");
                                    if (output != null && output.get("choices") != null) {
                                        List<Map<String, Object>> choices = (List<Map<String, Object>>) output.get("choices");
                                        if (!choices.isEmpty()) {
                                            Map<String, Object> firstChoice = choices.get(0);
                                            List<String> tokens = (List<String>) firstChoice.get("tokens");
                                            if (tokens != null) {
                                                String chunk = String.join("", tokens);
                                                fullText.append(chunk); // ✅ 누적
                                                return chunk;
                                            }
                                        }
                                    }
                                    System.out.println("Warning: tokens not found in item: " + item);
                                    return "";
                                });
                        }
                        return Flux.empty();
                    })
                    .doOnNext(chunk -> System.out.println("Stream output: " + chunk))
                    .doOnComplete(() -> System.out.println("📦 전체 결과:\n" + fullText)); // ✅ 마지막에 전체 출력
        });
    }

    /**
     * 추가: job 상태 확인 메서드 (필요 시 사용)
     */
    @GetMapping("/status/{endpointId}/{jobId}")
    public Mono<Map> checkJobStatus(@PathVariable String endpointId, @PathVariable String jobId) {
        String url = String.format("/v2/%s/status/%s", endpointId, jobId);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class);
    }
}
