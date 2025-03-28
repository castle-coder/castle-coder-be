package longrun.runpod.service;

import longrun.runpod.client.RunPodApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RunPodService {

    private final RunPodApiClient apiClient;

    @Value("${runpod.api.key}")
    private String runpodApiKey;

    @Value("${runpod.api.endpoint}")
    private String runpodEndPoint;

    // 고정된 endpointId 사용 (예: "quwzbmj4k9r5pi")
    public RunPodService(@Value("${runpod.api.key}") String apiKey) {
        String fixedEndpointId = "quwzbmj4k9r5pi";
        this.apiClient = new RunPodApiClient(apiKey, fixedEndpointId);
    }

    // Job 제출
    public Mono<Map> submitJob(String prompt) {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("prompt", prompt);
        inputMap.put("stream", true);

        Map<String, Object> samplingParams = new HashMap<>();
        samplingParams.put("max_tokens", 500);
        inputMap.put("sampling_params", samplingParams);

        Map<String, Object> payload = new HashMap<>();
        payload.put("input", inputMap);

        return apiClient.submitJob(payload);
    }

    // 스트리밍 Job 실행 및 결과 누적
    public Flux<String> streamJob(String prompt) {
        return submitJob(prompt).flatMapMany(response -> {
            Object idObj = response.get("id");
            if (idObj == null) {
                return Flux.error(new RuntimeException("Failed to get a job ID from the response."));
            }
            String jobId = idObj.toString();
            System.out.println("Job ID: " + jobId);

            StringBuilder fullText = new StringBuilder();

            return apiClient.pollStream(jobId)
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
                                                    fullText.append(chunk);
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
                    .doOnComplete(() -> System.out.println("📦 전체 결과:\n" + fullText));
        });
    }

    // Job 상태 확인
    public Mono<Map> checkJobStatus(String jobId) {
        return apiClient.checkJobStatus(jobId);
    }
}
