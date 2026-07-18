package com.careercompass.backend.certification.service;

import com.careercompass.backend.certification.dto.CertificationResponse;
import com.careercompass.backend.certification.dto.EnrollCertificationRequest;
import com.careercompass.backend.certification.dto.UpdateCertificationStatusRequest;
import com.careercompass.backend.certification.dto.UserCertificationResponse;
import com.careercompass.backend.certification.entity.Certification;
import com.careercompass.backend.certification.entity.UserCertification;
import com.careercompass.backend.certification.repository.CertificationRepository;
import com.careercompass.backend.certification.repository.UserCertificationRepository;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;
    private final UserCertificationRepository userCertificationRepository;
    private final UserRepository userRepository;

    @Override
    public List<CertificationResponse> getAllCertifications() {
        return certificationRepository.findAll()
                .stream()
                .map(this::mapToCertificationResponse)
                .toList();
    }

    @Override
    public CertificationResponse getCertificationById(Long id) {
        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certification not found with id: " + id));
        return mapToCertificationResponse(certification);
    }

    @Override
    public List<UserCertificationResponse> getMyCertifications() {
        Long userId = getCurrentUserId();
        return userCertificationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToUserCertificationResponse)
                .toList();
    }

    @Override
    public UserCertificationResponse enrollInCertification(
            EnrollCertificationRequest request) {

        Long userId = getCurrentUserId();
        if (userCertificationRepository.existsByUserIdAndCertificationId(
                userId, request.getCertificationId())) {
            throw new IllegalArgumentException(
                    "You are already enrolled in this certification.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        Certification certification = certificationRepository
                .findById(request.getCertificationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certification not found with id: "
                                + request.getCertificationId()));

        UserCertification userCertification = UserCertification.builder()
                .user(user)
                .certification(certification)
                .status("IN_PROGRESS")
                .completedAt(null)
                .build();

        UserCertification saved =
                userCertificationRepository.save(userCertification);
        return mapToUserCertificationResponse(saved);
    }

    @Override
    public UserCertificationResponse updateCertificationStatus(
            Long userCertificationId,
            UpdateCertificationStatusRequest request) {

        Long userId = getCurrentUserId();
        UserCertification userCertification =
                userCertificationRepository.findById(userCertificationId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Certification enrollment not found."));
        if (!userCertification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Certification enrollment not found.");
        }

        if (!request.getStatus().equals("IN_PROGRESS") &&
                !request.getStatus().equals("COMPLETED")) {
            throw new IllegalArgumentException(
                    "Status must be IN_PROGRESS or COMPLETED.");
        }

        userCertification.setStatus(request.getStatus());
        if ("COMPLETED".equals(request.getStatus())) {
            userCertification.setCompletedAt(
                    request.getCompletedAt() != null
                            ? request.getCompletedAt()
                            : java.time.LocalDate.now());
        } else {
            userCertification.setCompletedAt(null);
        }

        UserCertification updated =
                userCertificationRepository.save(userCertification);
        return mapToUserCertificationResponse(updated);
    }

    @Override
    public void removeCertification(Long userCertificationId) {
        Long userId = getCurrentUserId();

        UserCertification userCertification =
                userCertificationRepository.findById(userCertificationId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Certification enrollment not found."));
        if (!userCertification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Certification enrollment not found.");
        }

        userCertificationRepository.delete(userCertification);
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }

    private CertificationResponse mapToCertificationResponse(
            Certification cert) {
        List<String> skills = cert.getRelevantSkills() != null
                ? Arrays.stream(cert.getRelevantSkills().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList()
                : List.of();

        return CertificationResponse.builder()
                .id(cert.getId())
                .name(cert.getName())
                .provider(cert.getProvider())
                .url(cert.getUrl())
                .relevantSkills(skills)
                .build();
    }

    private UserCertificationResponse mapToUserCertificationResponse(
            UserCertification uc) {
        return UserCertificationResponse.builder()
                .id(uc.getId())
                .certificationId(uc.getCertification().getId())
                .certificationName(uc.getCertification().getName())
                .provider(uc.getCertification().getProvider())
                .url(uc.getCertification().getUrl())
                .status(uc.getStatus())
                .completedAt(uc.getCompletedAt())
                .build();
    }
}