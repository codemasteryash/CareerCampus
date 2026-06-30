package com.careercompass.backend.ai;

public interface AiClient {

    String chat(String prompt);

    String chat(String systemPrompt, String userMessage);
}