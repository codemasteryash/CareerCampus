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
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

    private final UserCertificationRepository userCertificationRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> REAL_CERT_URLS = Map.ofEntries(
            // AWS
            Map.entry("AWS Certified Solutions Architect – Associate", "https://aws.amazon.com/certification/certified-solutions-architect-associate/"),
            Map.entry("AWS Certified Developer – Associate", "https://aws.amazon.com/certification/certified-developer-associate/"),
            Map.entry("AWS Certified Cloud Practitioner", "https://aws.amazon.com/certification/certified-cloud-practitioner/"),
            Map.entry("AWS Certified Solutions Architect – Professional", "https://aws.amazon.com/certification/certified-solutions-architect-professional/"),
            Map.entry("AWS Certified DevOps Engineer – Professional", "https://aws.amazon.com/certification/certified-devops-engineer-professional/"),
            // Google Cloud
            Map.entry("Google Professional Cloud Architect", "https://cloud.google.com/learn/certification/cloud-architect"),
            Map.entry("Google Associate Cloud Engineer", "https://cloud.google.com/learn/certification/cloud-engineer"),
            Map.entry("Google Professional Data Engineer", "https://cloud.google.com/learn/certification/data-engineer"),
            Map.entry("Google Professional Cloud Developer", "https://cloud.google.com/learn/certification/cloud-developer"),
            // Azure
            Map.entry("Microsoft Azure Fundamentals AZ-900", "https://learn.microsoft.com/en-us/certifications/azure-fundamentals/"),
            Map.entry("Microsoft Azure Administrator AZ-104", "https://learn.microsoft.com/en-us/certifications/azure-administrator/"),
            Map.entry("Microsoft Azure Developer AZ-204", "https://learn.microsoft.com/en-us/certifications/azure-developer/"),
            Map.entry("Microsoft Certified: Azure Solutions Architect", "https://learn.microsoft.com/en-us/certifications/azure-solutions-architect/"),
            // Oracle Java
            Map.entry("Oracle Certified Professional Java SE 17 Developer", "https://education.oracle.com/oracle-certified-professional-java-se-17-developer/trackp_OCPJSE17"),
            Map.entry("Oracle Certified Associate Java SE 8", "https://education.oracle.com/oracle-certified-associate-java-se-8-programmer/trackp_333"),
            Map.entry("Oracle Certified Professional Java SE 11 Developer", "https://education.oracle.com/oracle-certified-professional-java-se-11-developer/trackp_815"),
            // Spring
            Map.entry("VMware Spring Professional", "https://www.vmware.com/learning/certification/spring-pro-develop-exam.html"),
            Map.entry("Spring Professional Certification", "https://www.vmware.com/learning/certification/spring-pro-develop-exam.html"),
            // Docker / Kubernetes
            Map.entry("Docker Certified Associate", "https://training.mirantis.com/certification/dca-certification-exam/"),
            Map.entry("Certified Kubernetes Administrator (CKA)", "https://training.linuxfoundation.org/certification/certified-kubernetes-administrator-cka/"),
            Map.entry("Certified Kubernetes Application Developer (CKAD)", "https://training.linuxfoundation.org/certification/certified-kubernetes-application-developer-ckad-2/"),
            // Terraform
            Map.entry("HashiCorp Certified: Terraform Associate", "https://www.hashicorp.com/certifications/terraform-associate"),
            // Databases
            Map.entry("MongoDB Certified Developer Associate", "https://learn.mongodb.com/pages/mongodb-associate-developer-exam"),
            Map.entry("MongoDB Associate Database Administrator", "https://learn.mongodb.com/pages/mongodb-associate-dba-exam"),
            Map.entry("PostgreSQL Association Certification", "https://www.postgresql.org/about/sponsors/"),
            // Security
            Map.entry("CompTIA Security+", "https://www.comptia.org/certifications/security"),
            Map.entry("Certified Ethical Hacker (CEH)", "https://www.eccouncil.org/train-certify/certified-ethical-hacker-ceh/"),
            Map.entry("CISSP", "https://www.isc2.org/Certifications/CISSP"),
            // Data / AI / ML
            Map.entry("TensorFlow Developer Certificate", "https://www.tensorflow.org/certificate"),
            Map.entry("AWS Certified Machine Learning – Specialty", "https://aws.amazon.com/certification/certified-machine-learning-specialty/"),
            Map.entry("Google Professional Machine Learning Engineer", "https://cloud.google.com/learn/certification/machine-learning-engineer"),
            Map.entry("Microsoft Azure AI Engineer Associate", "https://learn.microsoft.com/en-us/certifications/azure-ai-engineer/"),
            // DevOps
            Map.entry("GitLab Certified CI/CD Associate", "https://about.gitlab.com/handbook/customer-success/professional-services-engineering/gitlab-certified-associate/"),
            Map.entry("Jenkins Engineer Certification", "https://www.cloudbees.com/jenkins/jenkins-certification"),
            Map.entry("CompTIA Linux+", "https://www.comptia.org/certifications/linux"),
            // React / Frontend
            Map.entry("Meta Front-End Developer Professional Certificate", "https://www.coursera.org/professional-certificates/meta-front-end-developer"),
            Map.entry("Meta React Developer Certificate", "https://www.coursera.org/professional-certificates/meta-react-native"),
            // Scrum / Agile
            Map.entry("Professional Scrum Master (PSM I)", "https://www.scrum.org/assessments/professional-scrum-master-i-certification"),
            Map.entry("PMI Agile Certified Practitioner (PMI-ACP)", "https://www.pmi.org/certifications/agile-acp")
    );

    @Override
    public List<AiCertificationRecommendation> getRecommendations() {
        Long userId = getCurrentUserId();

        List<String> userSkills = userSkillRepository
                .findByUserId(userId)
                .stream()
                .map(us -> us.getSkill().getName())
                .toList();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String targetRole = user.getTargetJobRole() != null
                ? user.getTargetJobRole() : "Software Developer";

        String availableCerts = String.join(", ", REAL_CERT_URLS.keySet());

        String prompt = """
                You are a career advisor recommending real, current certifications.
                
                Target role: %s
                Current skills: %s
                
                Choose exactly 5 certifications from this list of real certifications:
                %s
                
                For each, return a JSON array. Use the EXACT name from the list above.
                Return ONLY valid JSON, no markdown, no backticks, no explanation:
                [
                  {
                    "name": "exact certification name from the list",
                    "provider": "the organization that offers it",
                    "reason": "one sentence why this specific cert helps for the target role",
                    "difficulty": "BEGINNER or INTERMEDIATE or ADVANCED"
                  }
                ]
                """.formatted(targetRole, String.join(", ", userSkills), availableCerts);

        try {
            String aiResponse = aiClient.chat(prompt);
            String cleaned = aiResponse.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();
            int start = cleaned.indexOf('[');
            if (start > 0) cleaned = cleaned.substring(start);
            int end = cleaned.lastIndexOf(']');
            if (end > 0 && end < cleaned.length() - 1) cleaned = cleaned.substring(0, end + 1);

            List<AiCertificationRecommendation> recs = objectMapper.readValue(
                    cleaned, new TypeReference<>() {});

            return recs.stream().map(rec -> {
                String realUrl = REAL_CERT_URLS.entrySet().stream()
                        .filter(e -> e.getKey().toLowerCase()
                                .contains(rec.getName().toLowerCase().substring(0,
                                        Math.min(20, rec.getName().length()))))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(REAL_CERT_URLS.getOrDefault(rec.getName(),
                                "https://www.google.com/search?q=" +
                                        rec.getName().replace(" ", "+") + "+certification+official"));

                return AiCertificationRecommendation.builder()
                        .name(rec.getName())
                        .provider(rec.getProvider())
                        .url(realUrl)
                        .reason(rec.getReason())
                        .difficulty(rec.getDifficulty())
                        .build();
            }).toList();

        } catch (Exception e) {
            log.error("Failed to parse certification recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI certification recommendations: " + e.getMessage());
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
    public UserCertificationResponse enrollInCertification(EnrollCertificationRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        boolean alreadyEnrolled = userCertificationRepository
                .findByUserId(userId)
                .stream()
                .anyMatch(uc -> uc.getCertificationName()
                        .equalsIgnoreCase(request.getCertificationName()));

        if (alreadyEnrolled) {
            throw new IllegalArgumentException("You are already tracking this certification.");
        }

        UserCertification userCertification = UserCertification.builder()
                .user(user)
                .certificationName(request.getCertificationName())
                .provider(request.getProvider())
                .url(request.getUrl())
                .status("IN_PROGRESS")
                .completedAt(null)
                .build();

        return mapToResponse(userCertificationRepository.save(userCertification));
    }

    @Override
    public UserCertificationResponse updateCertificationStatus(
            Long userCertificationId, UpdateCertificationStatusRequest request) {

        Long userId = getCurrentUserId();
        UserCertification uc = userCertificationRepository.findById(userCertificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification enrollment not found."));

        if (!uc.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Certification enrollment not found.");
        }

        if (!request.getStatus().equals("IN_PROGRESS") && !request.getStatus().equals("COMPLETED")) {
            throw new IllegalArgumentException("Status must be IN_PROGRESS or COMPLETED.");
        }

        uc.setStatus(request.getStatus());
        if ("COMPLETED".equals(request.getStatus())) {
            uc.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : LocalDate.now());
        } else {
            uc.setCompletedAt(null);
        }

        return mapToResponse(userCertificationRepository.save(uc));
    }

    @Override
    public void removeCertification(Long userCertificationId) {
        Long userId = getCurrentUserId();
        UserCertification uc = userCertificationRepository.findById(userCertificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification enrollment not found."));

        if (!uc.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Certification enrollment not found.");
        }
        userCertificationRepository.delete(uc);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
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