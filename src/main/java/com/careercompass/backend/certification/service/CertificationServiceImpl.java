package com.careercompass.backend.certification.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.certification.dto.AiCertificationRecommendation;
import com.careercompass.backend.certification.dto.EnrollCertificationRequest;
import com.careercompass.backend.certification.dto.UpdateCertificationStatusRequest;
import com.careercompass.backend.certification.dto.UserCertificationResponse;
import com.careercompass.backend.certification.entity.UserCertification;
import com.careercompass.backend.certification.repository.UserCertificationRepository;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.entity.UserSkill;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

    private final UserCertificationRepository userCertificationRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiCertificationRecommendation> getRecommendations() {
        Long userId = getCurrentUserId();

        List<String> userSkills = userSkillRepository
                .findByUserId(userId)
                .stream()
                .map(us -> us.getSkill().getName())
                .toList();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        String targetRole = user.getTargetJobRole() != null
                ? user.getTargetJobRole()
                : "Software Developer";

        String prompt = """
                You are a career advisor. Recommend 5 certifications
                for someone targeting the role: %s
                
                Their current skills are: %s
                
                Return ONLY a valid JSON array with no markdown,
                no backticks, no explanation. Exactly this format:
                [
                  {
                    "name": "certification name",
                    "provider": "provider name",
                    "url": "official certification url",
                    "reason": "one sentence why this helps",
                    "difficulty": "BEGINNER or INTERMEDIATE or ADVANCED"
                  }
                ]
                """.formatted(targetRole, String.join(", ", userSkills));

        String aiResponse = aiClient.chat(prompt);
        try {
            return objectMapper.readValue(
                    aiResponse,
                    new TypeReference<List<AiCertificationRecommendation>>() {});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse AI certification recommendations: "
                            + e.getMessage());
        }
    }

    @Override
    public List<UserCertificationResponse> getMyCertifications() {
        Long userId = getCurrentUserId();
        return userCertificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserCertificationResponse enrollInCertification(
            EnrollCertificationRequest request) {

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        boolean alreadyEnrolled = userCertificationRepository
                .findByUserId(userId)
                .stream()
                .anyMatch(uc -> uc.getCertificationName()
                        .equalsIgnoreCase(request.getCertificationName()));

        if (alreadyEnrolled) {
            throw new IllegalArgumentException(
                    "You are already tracking this certification.");
        }

        UserCertification userCertification = UserCertification.builder()
                .user(user)
                .certificationName(request.getCertificationName())
                .provider(request.getProvider())
                .url(request.getUrl())
                .status("IN_PROGRESS")
                .completedAt(null)
                .build();

        return mapToResponse(
                userCertificationRepository.save(userCertification));
    }

    @Override
    public UserCertificationResponse updateCertificationStatus(
            Long userCertificationId,
            UpdateCertificationStatusRequest request) {

        Long userId = getCurrentUserId();

        UserCertification uc = userCertificationRepository
                .findById(userCertificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certification enrollment not found."));

        if (!uc.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Certification enrollment not found.");
        }

        if (!request.getStatus().equals("IN_PROGRESS") &&
                !request.getStatus().equals("COMPLETED")) {
            throw new IllegalArgumentException(
                    "Status must be IN_PROGRESS or COMPLETED.");
        }

        uc.setStatus(request.getStatus());

        if ("COMPLETED".equals(request.getStatus())) {
            uc.setCompletedAt(request.getCompletedAt() != null
                    ? request.getCompletedAt()
                    : LocalDate.now());
        } else {
            uc.setCompletedAt(null);
        }

        return mapToResponse(userCertificationRepository.save(uc));
    }

    @Override
    public void removeCertification(Long userCertificationId) {
        Long userId = getCurrentUserId();

        UserCertification uc = userCertificationRepository
                .findById(userCertificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certification enrollment not found."));

        if (!uc.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Certification enrollment not found.");
        }

        userCertificationRepository.delete(uc);
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }

    private UserCertificationResponse mapToResponse(UserCertification uc) {
        return UserCertificationResponse.builder()
                .id(uc.getId())
                .certificationName(uc.getCertificationName())
                .provider(uc.getProvider())
                .url(uc.getUrl())
                .status(uc.getStatus())
                .completedAt(uc.getCompletedAt())
                .build();
    }
}