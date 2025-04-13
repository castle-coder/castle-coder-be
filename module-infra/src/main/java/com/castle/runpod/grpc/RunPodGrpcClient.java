package com.castle.runpod.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.http.*;
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

    public RunPodGrpcClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String runAndGetGrpcTarget(String endpointId, String apiKey, String prompt) {
        String runUrl = "https://api.runpod.ai/v2/" + endpointId + "/run";
        String statusUrlTemplate = "https://api.runpod.ai/v2/" + endpointId + "/status/%s";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> input = new HashMap<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are Qwen, created by Alibaba Cloud. You are a helpful assistant.");

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        input.put("messages", List.of(systemMessage, userMessage));

        Map<String, Object> samplingParams = new HashMap<>();
        samplingParams.put("temperature", 0.7);
        samplingParams.put("max_tokens", 10000);
        input.put("sampling_params", samplingParams);
        input.put("stream", true);

        requestBody.put("input", input);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(runUrl, requestEntity, String.class);
        String requestId;
        try {
            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            requestId = root.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse request id from RunPod", e);
        }

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

        stub.handle(request, new StreamObserver<HandleResponse>() {
            @Override
            public void onNext(HandleResponse response) {
                try {
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
                channel.shutdown();
            }
        });
    }
}
