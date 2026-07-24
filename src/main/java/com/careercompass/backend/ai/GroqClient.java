package com.careercompass.backend.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroqClient implements AiClient {

    private final ChatClient chatClient;

    public GroqClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String prompt) {
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
        ));

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }
    public static String cleanJsonResponse(String raw) {
        if (raw == null) return "[]";
        String cleaned = raw.trim()
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
        int arrayStart = cleaned.indexOf('[');
        int objStart = cleaned.indexOf('{');
        if (arrayStart == -1 && objStart == -1) return cleaned;
        if (arrayStart == -1) return cleaned.substring(objStart);
        if (objStart == -1) return cleaned.substring(arrayStart);
        return cleaned.substring(Math.min(arrayStart, objStart));
    }
}
