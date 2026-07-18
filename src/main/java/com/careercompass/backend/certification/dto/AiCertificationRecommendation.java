package com.careercompass.backend.certification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCertificationRecommendation {
    private String name;
    private String provider;
    private String url;
    private String reason;
    private String difficulty;
}