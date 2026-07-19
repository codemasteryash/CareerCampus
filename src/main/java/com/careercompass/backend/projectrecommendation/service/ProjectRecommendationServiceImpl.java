package com.careercompass.backend.projectrecommendation.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.projectrecommendation.dto.ProjectResponse;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectRecommendationServiceImpl implements ProjectRecommendationService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProjectResponse> getRecommendations() {
        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        List<String> userSkills = userSkillRepository
                .findByUserId(userId)
                .stream()
                .map(us -> us.getSkill().getName())
                .toList();

        String targetRole = user.getTargetJobRole() != null
                ? user.getTargetJobRole()
                : "Software Developer";

        String prompt = """
                You are a senior software engineer and career mentor.
                Recommend 5 hands-on projects for someone targeting
                the role: %s
                
                Their current skills are: %s
                
                Each project should help them build practical experience
                and strengthen their portfolio for this target role.
                
                Return ONLY a valid JSON array with no markdown,
                no backticks, no explanation. Exactly this format:
                [
                  {
                    "title": "project title",
                    "description": "2-3 sentence project description",
                    "skillsCovered": ["skill1", "skill2", "skill3"],
                    "difficulty": "BEGINNER or INTERMEDIATE or ADVANCED",
                    "estimatedHours": 20,
                    "relevantJobRole": "%s",
                    "reason": "one sentence why this project helps"
                  }
                ]
                """.formatted(targetRole, String.join(", ", userSkills),
                targetRole);

        String aiResponse = aiClient.chat(prompt);

        try {
            return objectMapper.readValue(
                    aiResponse,
                    new TypeReference<List<ProjectResponse>>() {});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse AI project recommendations: "
                            + e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}