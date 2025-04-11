package com.castle.runpod.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import service.ServicePb2;
import service.ServicePb2Grpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class RunPodGrpcClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RunPodGrpcClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * RunPod AI 모델 서버에 REST 요청을 보낸 후, 워커의 gRPC 대상(ip:port)을 가져온다.
     */
    public String runAndGetGrpcTarget(String endpointId, String apiKey, String prompt) {
        String runUrl = "https://api.runpod.ai/v2/" + endpointId + "/run";
        String statusUrlTemplate = "https://api.runpod.ai/v2/" + endpointId + "/status/%s";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // POST 요청 본문 구성
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> input = new HashMap<>();

        // system, user 메시지 구성
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are Qwen, created by Alibaba Cloud. You are a helpful assistant.");

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        input.put("messages", List.of(systemMessage, userMessage));

        // 샘플링 파라미터
        Map<String, Object> samplingParams = new HashMap<>();
        samplingParams.put("temperature", 0.7);
        samplingParams.put("max_tokens", 10000);
        input.put("sampling_params", samplingParams);

        // 스트리밍 옵션
        input.put("stream", true);
        requestBody.put("input", input);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // RunPod에 POST 요청
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(runUrl, requestEntity, String.class);
        String requestId;
        try {
            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            requestId = root.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse request id from RunPod", e);
        }

        // 워커 준비 상태를 폴링하여 gRPC 대상(ip:port) 반환
        String grpcTarget = null;
        for (int i = 0; i < 300; i++) {
            String statusUrl = String.format(statusUrlTemplate, requestId);
            ResponseEntity<String> statusResponse = restTemplate.getForEntity(statusUrl, String.class);
            try {
                JsonNode statusJson = objectMapper.readTree(statusResponse.getBody());
                JsonNode output = statusJson.path("output");
                if (output.path("status").asText().equals("start")) {
                    String ip = output.path("ip").asText();
                    String port = output.path("port").asText();
                    grpcTarget = ip + ":" + port;
                    break;
                }
                if (statusJson.path("status").asText().equals("FAILED")) {
                    throw new RuntimeException("Worker execution failed");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error polling status", e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return grpcTarget;
    }

    /**
     * gRPC를 통해 AI 모델 서버의 스트리밍 응답을 받아, 콜백(onToken)을 통해 토큰 단위로 전달한다.
     */
    public void streamFromGrpc(String grpcTarget, String prompt,
                               Consumer<String> onToken,
                               Runnable onComplete,
                               Consumer<Throwable> onError) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(grpcTarget)
                .usePlaintext()
                .build();

        ServicePb2Grpc.RunPodServiceStub stub = ServicePb2Grpc.newStub(channel);

        ServicePb2.HandleRequest request = ServicePb2.HandleRequest.newBuilder()
                .setPrompt(prompt)
                .setSeconds(2)
                .build();

        stub.handle(request, new StreamObserver<ServicePb2.HandleResponse>() {
            @Override
            public void onNext(ServicePb2.HandleResponse response) {
                try {
                    JsonNode data = objectMapper.readTree(response.getResult());
                    if (data.has("finished") && data.get("finished").asBoolean()) {
                        onComplete.run();
                        return;
                    }
                    // token 추출: token → choices[0] → tokens[0]
                    JsonNode choices = data.path("token").path("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode tokens = choices.get(0).path("tokens");
                        if (tokens.isArray() && tokens.size() > 0) {
                            String token = tokens.get(0).asText();
                            onToken.accept(token);
                        }
                    }
                } catch (Exception ex) {
                    onError.accept(ex);
                }
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onCompleted() {
                onComplete.run();
            }
        });
    }
}
