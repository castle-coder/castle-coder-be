package com.castle.runpod.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import runpod.Service.HandleRequest;
import runpod.Service.HandleResponse;
import runpod.RunPodServiceGrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class RunPodGrpcClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${runpod.api.endpoint}")
    private String endpointId;

    @Value("${runpod.api.key}")
    private String apiKey;

    public RunPodGrpcClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String runAndGetGrpcTarget(String prompt) {
        String runUrl = buildRunUrl(endpointId);
        String statusUrlTemplate = buildStatusUrlTemplate(endpointId);

        String requestId = sendRunRequest(runUrl, prompt);
        return pollForGrpcTarget(statusUrlTemplate, requestId);
    }

    public void streamFromGrpc(String grpcTarget, String prompt,
                               Consumer<String> onToken,
                               Runnable onComplete,
                               Consumer<Throwable> onError) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(grpcTarget)
                .usePlaintext()
                .build();

        RunPodServiceGrpc.RunPodServiceStub stub = RunPodServiceGrpc.newStub(channel);

        HandleRequest request = HandleRequest.newBuilder()
                .setPrompt(prompt)
                .setSeconds(2)
                .build();

        stub.handle(request, new StreamObserver<>() {
            @Override
            public void onNext(HandleResponse response) {
                try {
                    handleResponse(response, onToken, onComplete);
                } catch (Exception ex) {
                    onError.accept(ex);
                }
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                onComplete.run();
                channel.shutdown();
            }
        });
    }

    private String buildRunUrl(String endpointId) {
        return "https://api.runpod.ai/v2/" + endpointId + "/run";
    }

    private String buildStatusUrlTemplate(String endpointId) {
        return "https://api.runpod.ai/v2/" + endpointId + "/status/%s";
    }

    private String sendRunRequest(String runUrl, String prompt) {
        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(buildRequestBody(prompt), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(runUrl, requestEntity, String.class);

        try {
            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            return root.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse request id from RunPod", e);
        }
    }

    private String pollForGrpcTarget(String statusUrlTemplate, String requestId) {
        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Void> statusRequestEntity = new HttpEntity<>(headers);

        for (int i = 0; i < 300; i++) {
            String statusUrl = String.format(statusUrlTemplate, requestId);
            try {
                ResponseEntity<String> statusResponse = restTemplate.exchange(
                        statusUrl, HttpMethod.GET, statusRequestEntity, String.class
                );
                JsonNode statusJson = objectMapper.readTree(statusResponse.getBody());
                JsonNode output = statusJson.path("output");

                if ("start".equals(output.path("status").asText())) {
                    String ip = output.path("ip").asText();
                    String port = output.path("port").asText();
                    return ip + ":" + port;
                }

                if ("FAILED".equals(statusJson.path("status").asText())) {
                    throw new RuntimeException("Worker execution failed");
                }

                Thread.sleep(1000); // wait 1s
            } catch (Exception e) {
                throw new RuntimeException("Error polling status", e);
            }
        }

        throw new RuntimeException("Timeout: Failed to get gRPC target in time");
    }

    private void handleResponse(HandleResponse response,
                                Consumer<String> onToken,
                                Runnable onComplete) throws Exception {
        JsonNode data = objectMapper.readTree(response.getResult());

        if (data.has("finished") && data.get("finished").asBoolean()) {
            onComplete.run();
            return;
        }

        JsonNode choices = data.path("token").path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode tokens = choices.get(0).path("tokens");
            if (tokens.isArray() && tokens.size() > 0) {
                String token = tokens.get(0).asText();
                onToken.accept(token);
            }
        }
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, String> systemMessage = Map.of(
                "role", "system",
                "content", "You are Qwen, created by Alibaba Cloud. You are a helpful assistant."
        );

        Map<String, String> userMessage = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> samplingParams = Map.of(
                "temperature", 0.01,
                "max_tokens", 2048
        );

        Map<String, Object> input = new HashMap<>();
        input.put("messages", List.of(systemMessage, userMessage));
        input.put("sampling_params", samplingParams);
        input.put("stream", true);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", input);
        return requestBody;
    }
}
