package com.careercompass.backend.dashboard.dto;

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
    private Integer readinessScore;
    private String readinessLevel;
    private Integer totalSkills;
    private List<String> topMissingSkills;
    private Integer certificationCount;
    private Integer totalRoadmapSteps;
    private Integer completedRoadmapSteps;
    private List<String> suggestedNextSteps;
}