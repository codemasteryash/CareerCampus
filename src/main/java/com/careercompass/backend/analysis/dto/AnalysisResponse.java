package com.careercompass.backend.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private SkillGapResponse skillGap;
    private ReadinessScoreResponse readinessScore;
    private String aiSummary;
}