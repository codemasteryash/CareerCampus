package com.careercompass.backend.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessScoreResponse {
    private String targetJobRole;
    private Integer score;
    private String level;
    private String feedback;
}