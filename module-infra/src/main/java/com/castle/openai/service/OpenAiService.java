package com.castle.openai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String generateImagePrompt(List<String> imageUrls) {
        try {
            // 이미지들을 base64로 변환
            StringBuilder imagePart = new StringBuilder();
            for (String imageUrl : imageUrls) {
                String base64Image = encodeImageToBase64(imageUrl);
                imagePart.append("{\"type\":\"image_url\",\"image_url\":{\"url\":\"data:image/jpeg;base64,")
                        .append(base64Image)
                        .append("\"}},");
            }

            // 마지막 콤마 제거
            if (imagePart.length() > 0) {
                imagePart.setLength(imagePart.length() - 1);
            }

            String requestBody = """
            {
              "model": "gpt-4-vision-preview",
              "messages": [
                {
                  "role": "user",
                  "content": [
                      { "type": "input_text", "text": "what's in this image?" },
                      {
                          "type": "input_image",
                          "image_url": f"data:image/jpeg;base64,{base64_image}",
                      },
                  ]
                }
              ],
              "max_tokens": 1000
            }
            """.formatted(imagePart.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("choices").get(0).path("message").path("content").asText();
            } else {
                return "OpenAI API error: " + response.getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to process images: " + e.getMessage();
        }
    }

    private String encodeImageToBase64(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        try (InputStream is = url.openStream()) {
            byte[] imageBytes = is.readAllBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }
}
