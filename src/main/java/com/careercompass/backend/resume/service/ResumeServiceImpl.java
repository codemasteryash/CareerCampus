package com.careercompass.backend.resume.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.exception.InvalidResumeException;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.resume.dto.ResumeAnalysisResponse;
import com.careercompass.backend.resume.dto.ResumeUploadResponse;
import com.careercompass.backend.resume.entity.Resume;
import com.careercompass.backend.resume.repository.ResumeRepository;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.entity.Skill;
import com.careercompass.backend.skill.entity.UserSkill;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.careercompass.backend.util.PdfParserUtil;
import com.careercompass.backend.util.SkillExtractorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final PdfParserUtil pdfParserUtil;
    private final SkillExtractorUtil skillExtractorUtil;
    private final AiClient aiClient;

    @Override
    public ResumeUploadResponse uploadResume(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidResumeException("Please upload a file.");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new InvalidResumeException(
                    "Only PDF files are supported.");
        }
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        String extractedText = pdfParserUtil.extractText(file);

        if (extractedText == null || extractedText.isBlank()) {
            throw new InvalidResumeException(
                    "Could not extract text from the PDF. " +
                            "Please ensure the PDF is not scanned/image-based.");
        }
        Resume resume = resumeRepository.findByUserId(userId)
                .orElse(new Resume());

        resume.setUser(user);
        resume.setOriginalFileName(originalFilename);
        resume.setFileStorageKey(userId + "_resume.pdf");
        resume.setExtractedText(extractedText);
        resume.setUploadedAt(LocalDateTime.now());

        Resume savedResume = resumeRepository.save(resume);
        List<String> extractedSkillNames = extractSkillsWithAi(extractedText);

        List<Skill> matchedSkills =
                skillExtractorUtil.matchSkillsFromCatalogue(extractedSkillNames);

        for (Skill skill : matchedSkills) {
            if (!userSkillRepository.existsByUserIdAndSkillId(
                    userId, skill.getId())) {
                UserSkill userSkill = UserSkill.builder()
                        .user(user)
                        .skill(skill)
                        .proficiencyLevel("INTERMEDIATE")
                        .source("RESUME_PARSED")
                        .build();
                userSkillRepository.save(userSkill);
            }
        }
        return ResumeUploadResponse.builder()
                .resumeId(savedResume.getId())
                .originalFileName(savedResume.getOriginalFileName())
                .uploadedAt(savedResume.getUploadedAt())
                .message("Resume uploaded successfully. " +
                        matchedSkills.size() + " skills extracted.")
                .build();
    }
    @Override
    public ResumeAnalysisResponse getResumeAnalysis() {
        Long userId = getCurrentUserId();

        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No resume found. Please upload your resume first."));

        List<String> extractedSkills = userSkillRepository
                .findByUserId(userId)
                .stream()
                .filter(us -> "RESUME_PARSED".equals(us.getSource()))
                .map(us -> us.getSkill().getName())
                .toList();

        String summary = aiClient.chat(
                "Summarize this resume in 2-3 sentences, " +
                        "focusing on key skills and experience level:\n\n"
                        + resume.getExtractedText());

        return ResumeAnalysisResponse.builder()
                .resumeId(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .extractedSkills(extractedSkills)
                .summary(summary)
                .build();
    }

    private List<String> extractSkillsWithAi(String resumeText) {
        String prompt = """
                Extract technical skills from this resume text.
                Return ONLY a comma-separated list of skill names.
                Example format: Java, Spring Boot, PostgreSQL, Docker
                Do not include explanations, numbers, or bullet points.
                Only include concrete technical skills.
                
                Resume text:
                """ + resumeText;

        String aiResponse = aiClient.chat(prompt);
        return Arrays.stream(aiResponse.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}