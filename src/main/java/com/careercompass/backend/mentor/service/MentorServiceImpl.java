package com.careercompass.backend.mentor.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.mentor.dto.ChatRequest;
import com.careercompass.backend.mentor.dto.ChatResponse;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final AiClient aiClient;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    @Override
    public ChatResponse chat(ChatRequest request) {
        Long userId = getCurrentUserId();

                User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        List<String> userSkills = userSkillRepository
                .findByUserId(userId)
                .stream()
                .map(us -> us.getSkill().getName())
                .collect(Collectors.toList());

        String targetRole = user.getTargetJobRole() != null
                ? user.getTargetJobRole()
                : "Software Developer";

        String systemPrompt = """
                You are an expert career mentor specializing in
                technology and software development careers.
                
                You are currently advising:
                Name: %s
                Target Role: %s
                Current Skills: %s
                
                Your mentoring style:
                - Be encouraging but realistic
                - Give specific, actionable advice
                - Reference their actual skills and target role
                  in your responses when relevant
                - Keep responses concise (3-5 sentences max
                  unless a detailed explanation is needed)
                - If asked about topics unrelated to career
                  development, gently redirect to career topics
                """.formatted(
                user.getName(),
                targetRole,
                userSkills.isEmpty()
                        ? "none added yet"
                        : String.join(", ", userSkills));

        String fullUserMessage = request.getMessage();

        if (request.getConversationHistory() != null
                && !request.getConversationHistory().isEmpty()) {
            String history = String.join("\n",
                    request.getConversationHistory());
            fullUserMessage = "Previous conversation:\n"
                    + history
                    + "\n\nCurrent message: "
                    + request.getMessage();
        }
        String reply = aiClient.chat(systemPrompt, fullUserMessage);

        return ChatResponse.builder()
                .reply(reply)
                .model("openai/gpt-oss-120b")
                .tokensUsed(null)
                .build();
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}