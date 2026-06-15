package com.careercompass.backend.dashboard.dto;

import com.careercompass.backend.analysis.dto.ReadinessScoreResponse;
import com.careercompass.backend.analysis.dto.SkillGapResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String userName;
    private String targetJobRole;
    private ReadinessScoreResponse readinessScore;
    private SkillGapResponse skillGap;
    private Integer totalRoadmapSteps;
    private Integer completedRoadmapSteps;
    private Integer certificationCount;
    private List<String> topMissingSkills;
    private List<String> suggestedNextSteps;
}