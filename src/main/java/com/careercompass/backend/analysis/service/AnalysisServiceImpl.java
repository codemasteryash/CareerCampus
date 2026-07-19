package com.careercompass.backend.analysis.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.analysis.dto.AnalysisResponse;
import com.careercompass.backend.analysis.dto.ReadinessScoreResponse;
import com.careercompass.backend.analysis.dto.SkillGapResponse;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.role.entity.RoleSkill;
import com.careercompass.backend.role.repository.JobRoleRepository;
import com.careercompass.backend.role.repository.RoleSkillRepository;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.careercompass.backend.util.ReadinessCalculatorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final JobRoleRepository jobRoleRepository;
    private final RoleSkillRepository roleSkillRepository;
    private final ReadinessCalculatorUtil readinessCalculatorUtil;
    private final AiClient aiClient;

    @Override
    public AnalysisResponse getFullAnalysis() {
        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        if (user.getTargetJobRole() == null
                || user.getTargetJobRole().isBlank()) {
            throw new IllegalArgumentException(
                    "Please set a target job role in your profile " +
                            "before running analysis.");
        }

        List<String> userSkillNames = userSkillRepository
                .findByUserId(userId)
                .stream()
                .map(us -> us.getSkill().getName())
                .toList();
        var jobRole = jobRoleRepository
                .findByTitle(user.getTargetJobRole())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Target job role '" + user.getTargetJobRole()
                                + "' not found in the system. " +
                                "Please update your profile with a valid role."));

        List<RoleSkill> roleSkills =
                roleSkillRepository.findByJobRoleId(jobRole.getId());


        List<String> mustHaveSkills = roleSkills.stream()
                .filter(rs -> "MUST_HAVE".equals(rs.getImportance()))
                .map(rs -> rs.getSkill().getName())
                .toList();

        List<String> niceToHaveSkills = roleSkills.stream()
                .filter(rs -> "NICE_TO_HAVE".equals(rs.getImportance()))
                .map(rs -> rs.getSkill().getName())
                .toList();


        List<String> presentSkills = userSkillNames.stream()
                .filter(skill -> mustHaveSkills.stream()
                        .anyMatch(s -> s.equalsIgnoreCase(skill))
                        || niceToHaveSkills.stream()
                        .anyMatch(s -> s.equalsIgnoreCase(skill)))
                .toList();

        List<String> missingMustHave = mustHaveSkills.stream()
                .filter(skill -> userSkillNames.stream()
                        .noneMatch(s -> s.equalsIgnoreCase(skill)))
                .toList();

        List<String> missingNiceToHave = niceToHaveSkills.stream()
                .filter(skill -> userSkillNames.stream()
                        .noneMatch(s -> s.equalsIgnoreCase(skill)))
                .toList();

        SkillGapResponse skillGap = SkillGapResponse.builder()
                .targetJobRole(user.getTargetJobRole())
                .presentSkills(presentSkills)
                .missingMustHaveSkills(missingMustHave)
                .missingNiceToHaveSkills(missingNiceToHave)
                .build();

        int score = readinessCalculatorUtil.calculateScore(
                userSkillNames, mustHaveSkills, niceToHaveSkills);
        String level = readinessCalculatorUtil.calculateLevel(score);

        String feedback = aiClient.chat(
                "A developer targeting the role of '"
                        + user.getTargetJobRole()
                        + "' has a job readiness score of " + score + "/100. "
                        + "Their level is: " + level + ". "
                        + "They are missing these must-have skills: "
                        + missingMustHave + ". "
                        + "Give one encouraging but honest sentence of feedback.");

        ReadinessScoreResponse readinessScore = ReadinessScoreResponse.builder()
                .targetJobRole(user.getTargetJobRole())
                .score(score)
                .level(level)
                .feedback(feedback)
                .build();

        String aiSummary = aiClient.chat(
                """
                You are a career advisor. Give a 3-sentence career
                assessment for a developer with these details:
                
                Target Role: %s
                Current Skills: %s
                Missing Must-Have Skills: %s
                Missing Nice-To-Have Skills: %s
                Readiness Score: %d/100 (%s)
                
                Be specific, encouraging, and actionable.
                Focus on the most impactful next steps.
                """.formatted(
                        user.getTargetJobRole(),
                        userSkillNames,
                        missingMustHave,
                        missingNiceToHave,
                        score,
                        level));

        return AnalysisResponse.builder()
                .skillGap(skillGap)
                .readinessScore(readinessScore)
                .aiSummary(aiSummary)
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