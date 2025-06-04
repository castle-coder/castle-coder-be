package com.castle.openai.service;

import com.castle.code.error.ChatErrorCode;
import com.castle.domain.chat.entity.ChatSession;
import com.castle.domain.chat.repository.ChatSessionRepository;
import com.castle.domain.chat.service.ChatService;
import com.castle.exception.ChatException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ChatSessionRepository chatSessionRepository;

    @Transactional
    public void generateTitle(Long chatSessionId, String prompt) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));
        if (chatSession.getTitle() != null) {
            return; // 이미 제목이 생성된 경우
        }
        try {
            // 1) 요청 본문 구성 ---------------------------------------------------
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", openAiModel);          // ex) gpt-4o-mini, gpt-4o, gpt-4-turbo
            body.put("max_tokens", 32);              // 제목은 짧으니 충분히 작게
            body.put("temperature", 0.5);

            ArrayNode messages = objectMapper.createArrayNode();

            ObjectNode system = objectMapper.createObjectNode();
            system.put("role", "system");
            system.put("content",
                    "Summarize the user's text into a concise, catchy title (≤ 10 words), using the same language as the prompt");
            messages.add(system);

            ObjectNode user = objectMapper.createObjectNode();
            user.put("role", "user");
            user.put("content", prompt);
            messages.add(user);

            body.set("messages", messages);

            // 2) HTTP 설정 -------------------------------------------------------
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<String> request =
                    new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            // 3) 요청/응답 --------------------------------------------------------
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    // 첫 번째 선택지의 메시지 → content
                    chatSession.updateTitle(choices.get(0)
                            .path("message")
                            .path("content")
                            .asText()
                            .trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
