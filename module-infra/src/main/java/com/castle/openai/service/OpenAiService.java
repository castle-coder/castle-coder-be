package com.castle.openai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.model}")
    private String openAiModel;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateImagePrompt(List<String> imageUrls) {
        try {
            // Create message content
            ArrayNode contentArray = objectMapper.createArrayNode();
            contentArray.add(objectMapper.createObjectNode()
                    .put("type", "input_text")
                    .put("text", "Convert this image to text."));

            for (String imageUrl : imageUrls) {
                ObjectNode imageNode = objectMapper.createObjectNode();
                imageNode.put("type", "input_image");
                imageNode.put("image_url", imageUrl);
                contentArray.add(imageNode);
            }

            // Wrap message
            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", "user");
            messageNode.set("content", contentArray);

            // Root JSON
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", openAiModel);
            requestBody.set("input", objectMapper.createArrayNode().add(messageNode));

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/responses",
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode outputArray = root.path("output");
                if (outputArray.isArray() && outputArray.size() > 0) {
                    JsonNode firstMessage = outputArray.get(0);
                    JsonNode contentResponseArray = firstMessage.path("content");
                    if (contentResponseArray.isArray() && contentResponseArray.size() > 0) {
                        return contentResponseArray.get(0).path("text").asText();
                    }
                }
                // output 배열이 없거나 비어있는 경우 대비
                return "";
            } else {
                return "OpenAI API error: " + response.getStatusCode();
            }


        } catch (Exception e) {
            e.printStackTrace();
            return "이미지 분석 중 오류 발생: " + e.getMessage();
        }
    }

}
