package com.careercompass.backend.mentor.service;

import com.careercompass.backend.mentor.dto.ChatRequest;
import com.careercompass.backend.mentor.dto.ChatResponse;

public interface MentorService {

    ChatResponse chat(ChatRequest request);
}