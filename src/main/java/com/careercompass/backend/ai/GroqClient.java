//package com.careercompass.backend.ai;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.messages.SystemMessage;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class GroqClient implements AiClient {
//
//    private final ChatClient chatClient;
//
//    public GroqClient(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }
//
//    @Override
//    public String chat(String prompt) {
//        return chatClient
//                .prompt()
//                .user(prompt)
//                .call()
//                .content();
//    }
//
//    @Override
//    public String chat(String systemPrompt, String userMessage) {
//        Prompt prompt = new Prompt(List.of(
//                new SystemMessage(systemPrompt),
//                new UserMessage(userMessage)
//        ));
//
//        return chatClient
//                .prompt(prompt)
//                .call()
//                .content();
//    }
//    public static String cleanJsonResponse(String raw) {
//        if (raw == null) return "[]";
//        String cleaned = raw.trim()
//                .replaceAll("```json\\s*", "")
//                .replaceAll("```\\s*", "")
//                .trim();
//        int arrayStart = cleaned.indexOf('[');
//        int objStart = cleaned.indexOf('{');
//        if (arrayStart == -1 && objStart == -1) return cleaned;
//        if (arrayStart == -1) return cleaned.substring(objStart);
//        if (objStart == -1) return cleaned.substring(arrayStart);
//        return cleaned.substring(Math.min(arrayStart, objStart));
//    }
//}

package com.careercompass.backend.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GroqClient implements AiClient {

    private final ChatClient primaryClient;
    private final RestClient fallbackRestClient;
    private final String fallbackModel;
    private final ObjectMapper objectMapper;
    private final boolean hasFallback;

    public GroqClient(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model:openai/gpt-oss-120b}") String model,
            @Value("${groq.api.key.fallback:}") String fallbackKey
    ) {
        this.primaryClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.fallbackModel = model;

        if (fallbackKey != null && !fallbackKey.isBlank()) {
            this.fallbackRestClient = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + fallbackKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();
            this.hasFallback = true;
            log.info("Groq fallback key configured via raw RestClient");
        } else {
            this.fallbackRestClient = null;
            this.hasFallback = false;
            log.info("No fallback Groq key — single key mode");
        }
    }

    @Override
    public String chat(String prompt) {
        try {
            return primaryClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            if (hasFallback && isRateLimitError(e)) {
                log.warn("Primary rate limited — using fallback RestClient");
                return callFallbackApi(null, prompt);
            }
            throw e;
        }
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
        ));
        try {
            return primaryClient.prompt(prompt).call().content();
        } catch (Exception e) {
            if (hasFallback && isRateLimitError(e)) {
                log.warn("Primary rate limited — using fallback RestClient");
                return callFallbackApi(systemPrompt, userMessage);
            }
            throw e;
        }
    }

    private String callFallbackApi(String systemPrompt, String userMessage) {
        try {
            List<Map<String, String>> messages = systemPrompt != null
                    ? List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage))
                    : List.of(
                    Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = Map.of(
                    "model", fallbackModel,
                    "messages", messages,
                    "temperature", 0.7
            );

            String response = fallbackRestClient.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0)
                    .path("message").path("content").asText();

        } catch (Exception e) {
            log.error("Fallback Groq call failed: {}", e.getMessage());
            throw new RuntimeException("Both primary and fallback AI calls failed: " + e.getMessage());
        }
    }

    private boolean isRateLimitError(Exception e) {
        String msg = e.getMessage();
        return msg != null && (
                msg.contains("429") ||
                        msg.contains("rate limit") ||
                        msg.contains("Rate limit") ||
                        msg.contains("quota")
        );
    }
}