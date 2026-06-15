package com.careercompass.backend.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {
    private Long resumeId;
    private String originalFileName;
    private List<String> extractedSkills;
    private String summary;            // short AI-generated summary of the resume
}