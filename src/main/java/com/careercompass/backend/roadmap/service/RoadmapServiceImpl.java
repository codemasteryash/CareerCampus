package com.careercompass.backend.roadmap.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.notification.service.NotificationService;
import com.careercompass.backend.roadmap.dto.ProgressResponse;
import com.careercompass.backend.roadmap.dto.RoadmapResponse;
import com.careercompass.backend.roadmap.dto.RoadmapStepResponse;
import com.careercompass.backend.roadmap.dto.UpdateProgressRequest;
import com.careercompass.backend.roadmap.entity.Roadmap;
import com.careercompass.backend.roadmap.entity.RoadmapStep;
import com.careercompass.backend.roadmap.entity.UserRoadmapProgress;
import com.careercompass.backend.roadmap.repository.RoadmapRepository;
import com.careercompass.backend.roadmap.repository.RoadmapStepRepository;
import com.careercompass.backend.roadmap.repository.UserRoadmapProgressRepository;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final UserRoadmapProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RoadmapResponse getMyRoadmap() {
        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        if (user.getTargetJobRole() == null
                || user.getTargetJobRole().isBlank()) {
            throw new IllegalArgumentException(
                    "Please set a target job role in your profile " +
                            "before viewing your roadmap.");
        }

        String targetRole = user.getTargetJobRole();

        Roadmap roadmap = roadmapRepository
                .findByJobRole(targetRole)
                .orElseGet(() -> generateAndSaveRoadmap(targetRole));

        List<RoadmapStep> steps =
                roadmapStepRepository.findByRoadmapIdOrderByStepOrderAsc(
                        roadmap.getId());

        List<UserRoadmapProgress> progressList =
                progressRepository.findByUserIdAndRoadmapId(
                        userId, roadmap.getId());


        Map<Long, String> progressMap = progressList.stream()
                .collect(Collectors.toMap(
                        p -> p.getRoadmapStep().getId(),
                        UserRoadmapProgress::getStatus));


        long completedSteps = progressList.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();


        List<RoadmapStepResponse> stepResponses = steps.stream()
                .map(step -> RoadmapStepResponse.builder()
                        .id(step.getId())
                        .title(step.getTitle())
                        .description(step.getDescription())
                        .stepOrder(step.getStepOrder())
                        .resourceUrl(step.getResourceUrl())
                        .estimatedDays(step.getEstimatedDays())
                        // Default to NOT_STARTED if no progress record exists
                        .status(progressMap.getOrDefault(
                                step.getId(), "NOT_STARTED"))
                        .build())
                .toList();

        return RoadmapResponse.builder()
                .id(roadmap.getId())
                .title(roadmap.getTitle())
                .jobRole(roadmap.getJobRole())
                .description(roadmap.getDescription())
                .totalSteps(roadmap.getTotalSteps())
                .completedSteps((int) completedSteps)
                .steps(stepResponses)
                .build();
    }

    @Override
    @Transactional
    public ProgressResponse updateStepProgress(
            UpdateProgressRequest request) {

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        RoadmapStep step = roadmapStepRepository
                .findByIdWithRoadmap(request.getStepId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Roadmap step not found with id: "
                                + request.getStepId()));

        if (!List.of("NOT_STARTED", "IN_PROGRESS", "COMPLETED")
                .contains(request.getStatus())) {
            throw new IllegalArgumentException(
                    "Status must be NOT_STARTED, IN_PROGRESS, " +
                            "or COMPLETED.");
        }

        UserRoadmapProgress progress = progressRepository
                .findByUserIdAndRoadmapStepId(userId, step.getId())
                .orElse(UserRoadmapProgress.builder()
                        .user(user)
                        .roadmap(step.getRoadmap())
                        .roadmapStep(step)
                        .build());

        progress.setStatus(request.getStatus());

        if ("COMPLETED".equals(request.getStatus())) {
            progress.setCompletedAt(LocalDateTime.now());
        } else {
            progress.setCompletedAt(null);
        }

        progressRepository.save(progress);

        if ("COMPLETED".equals(request.getStatus())) {
            User fullUser = userRepository.findById(userId)
                    .orElseThrow();
            notificationService.sendRoadmapStepCompletedEmail(
                    fullUser.getEmail(),
                    fullUser.getName(),
                    step.getTitle(),
                    step.getRoadmap().getTitle());
        }

        return ProgressResponse.builder()
                .roadmapId(step.getRoadmap().getId())
                .roadmapTitle(step.getRoadmap().getTitle())
                .stepId(step.getId())
                .stepTitle(step.getTitle())
                .status(request.getStatus())
                .message("Progress updated to: " + request.getStatus())
                .build();
    }


    private Roadmap generateAndSaveRoadmap(String targetRole) {
        String prompt = """
                You are a senior software engineering mentor.
                Create a detailed learning roadmap for someone
                targeting the role: %s
                
                Return ONLY a valid JSON object with no markdown,
                no backticks, no explanation. Exactly this format:
                {
                  "title": "roadmap title",
                  "description": "2 sentence roadmap overview",
                  "steps": [
                    {
                      "title": "step title",
                      "description": "what to learn in this step",
                      "stepOrder": 1,
                      "resourceUrl": "https://relevant-resource-url.com",
                      "estimatedDays": 14
                    }
                  ]
                }
                
                Include 8-10 steps covering the full learning journey
                from fundamentals to job-ready.
                Steps must be ordered logically — fundamentals first,
                advanced topics last.
                """.formatted(targetRole);

//        String aiResponse = aiClient.chat(prompt);

        try {
            String aiResponse = aiClient.chat(prompt);
            String cleaned = aiResponse.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();
            int start = cleaned.indexOf('{');
            if (start > 0) cleaned = cleaned.substring(start);
            AiRoadmapResponse aiRoadmap = objectMapper.readValue(
                    aiResponse, AiRoadmapResponse.class);

            Roadmap roadmap = Roadmap.builder()
                    .title(aiRoadmap.getTitle())
                    .jobRole(targetRole)
                    .description(aiRoadmap.getDescription())
                    .totalSteps(aiRoadmap.getSteps().size())
                    .steps(new ArrayList<>())
                    .build();

            Roadmap savedRoadmap = roadmapRepository.save(roadmap);


            List<RoadmapStep> steps = aiRoadmap.getSteps().stream()
                    .map(s -> RoadmapStep.builder()
                            .roadmap(savedRoadmap)
                            .title(s.getTitle())
                            .description(s.getDescription())
                            .stepOrder(s.getStepOrder())
                            .resourceUrl(s.getResourceUrl())
                            .estimatedDays(s.getEstimatedDays())
                            .build())
                    .toList();

            roadmapStepRepository.saveAll(steps);

            return savedRoadmap;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate roadmap: " + e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AiRoadmapResponse {
        private String title;
        private String description;
        private List<AiRoadmapStep> steps;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AiRoadmapStep {
        private String title;
        private String description;
        private Integer stepOrder;
        private String resourceUrl;
        private Integer estimatedDays;
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}