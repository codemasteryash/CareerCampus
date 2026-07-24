package com.careercompass.backend.dashboard.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.certification.repository.UserCertificationRepository;
import com.careercompass.backend.dashboard.dto.DashboardResponse;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.role.entity.RoleSkill;
import com.careercompass.backend.role.repository.JobRoleRepository;
import com.careercompass.backend.role.repository.RoleSkillRepository;
import com.careercompass.backend.roadmap.repository.RoadmapRepository;
import com.careercompass.backend.roadmap.repository.UserRoadmapProgressRepository;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.careercompass.backend.util.ReadinessCalculatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserCertificationRepository userCertificationRepository;
    private final JobRoleRepository jobRoleRepository;
    private final RoleSkillRepository roleSkillRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRoadmapProgressRepository progressRepository;
    private final ReadinessCalculatorUtil readinessCalculatorUtil;
    private final AiClient aiClient;

    @Override
    public DashboardResponse getDashboard() {
        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        String targetRole = user.getTargetJobRole() != null
                ? user.getTargetJobRole()
                : null;

        List<String> userSkillNames = userSkillRepository
                .findByUserId(userId)
                .stream()
                .map(us -> us.getSkill().getName())
                .collect(Collectors.toList());
        int readinessScore = 0;
        String readinessLevel = "Not calculated";
        List<String> topMissingSkills = List.of();
        List<String> mustHaveSkills = List.of();
        List<String> niceToHaveSkills = List.of();

        if (targetRole != null) {
            var jobRoleOpt = jobRoleRepository.findByTitle(targetRole);

            if (jobRoleOpt.isPresent()) {
                List<RoleSkill> roleSkills = roleSkillRepository
                        .findByJobRoleId(jobRoleOpt.get().getId());

                mustHaveSkills = roleSkills.stream()
                        .filter(rs -> "MUST_HAVE".equals(rs.getImportance()))
                        .map(rs -> rs.getSkill().getName())
                        .collect(Collectors.toList());

                niceToHaveSkills = roleSkills.stream()
                        .filter(rs -> "NICE_TO_HAVE".equals(rs.getImportance()))
                        .map(rs -> rs.getSkill().getName())
                        .collect(Collectors.toList());

                topMissingSkills = mustHaveSkills.stream()
                        .filter(skill -> userSkillNames.stream()
                                .noneMatch(s -> s.equalsIgnoreCase(skill)))
                        .limit(5)
                        .collect(Collectors.toList());

                readinessScore = readinessCalculatorUtil.calculateScore(
                        userSkillNames, mustHaveSkills, niceToHaveSkills);
                readinessLevel = readinessCalculatorUtil
                        .calculateLevel(readinessScore);
            }
        }

        long certificationCount = userCertificationRepository
                .countByUserIdAndStatus(userId, "COMPLETED");

        int totalRoadmapSteps = 0;
        int completedRoadmapSteps = 0;

        if (targetRole != null) {
            var roadmapOpt = roadmapRepository.findByJobRole(targetRole);

            if (roadmapOpt.isPresent()) {
                totalRoadmapSteps = roadmapOpt.get().getTotalSteps();
                completedRoadmapSteps = (int) progressRepository
                        .countByUserIdAndRoadmapIdAndStatus(
                                userId,
                                roadmapOpt.get().getId(),
                                "COMPLETED");
            }
        }

        List<String> suggestedNextSteps = getSuggestedNextSteps(
                user.getName(),
                targetRole,
                userSkillNames,
                topMissingSkills,
                completedRoadmapSteps,
                totalRoadmapSteps);

        return DashboardResponse.builder()
                .userName(user.getName())
                .targetJobRole(targetRole)
                .readinessScore(readinessScore)
                .readinessLevel(readinessLevel)
                .totalSkills(userSkillNames.size())
                .topMissingSkills(topMissingSkills)
                .certificationCount((int) certificationCount)
                .totalRoadmapSteps(totalRoadmapSteps)
                .completedRoadmapSteps(completedRoadmapSteps)
                .suggestedNextSteps(suggestedNextSteps)
                .build();
    }


    private List<String> getSuggestedNextSteps(
            String name,
            String targetRole,
            List<String> currentSkills,
            List<String> missingSkills,
            int completedSteps,
            int totalSteps) {

        if (targetRole == null) {
            return List.of(
                    "Set your target job role in your profile",
                    "Upload your resume for skill extraction",
                    "Browse available job roles to find your target");
        }

        String prompt = """
                Give exactly 3 specific next-step action items for
                a developer named %s targeting: %s
                
                Current skills: %s
                Missing must-have skills: %s
                Roadmap progress: %d/%d steps completed
                
                Return ONLY a JSON array of 3 short action strings.
                Each string must be under 15 words.
                No markdown, no backticks, no explanation.
                Example: ["Learn PostgreSQL basics", "Build a REST API project", "Complete Docker step in roadmap"]
                """.formatted(
                name,
                targetRole,
                currentSkills.isEmpty() ? "none yet" :
                        String.join(", ", currentSkills),
                missingSkills.isEmpty() ? "none" :
                        String.join(", ", missingSkills),
                completedSteps,
                totalSteps);

        try {
            String aiResponse = aiClient.chat(prompt);
            String cleaned = aiResponse.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();
            int start = cleaned.indexOf('[');
            if (start > 0) cleaned = cleaned.substring(start);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    cleaned,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {

            return List.of(
                    "Complete your roadmap steps",
                    "Add missing skills to your profile",
                    "Enroll in a recommended certification");
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