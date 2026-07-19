package com.careercompass.backend.projectrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private String title;
    private String description;
    private List<String> skillsCovered;
    private String difficulty;
    private Integer estimatedHours;
    private String relevantJobRole;
    private String reason;
}