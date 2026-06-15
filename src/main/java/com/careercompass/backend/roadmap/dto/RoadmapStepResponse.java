package com.careercompass.backend.roadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapStepResponse {
    private Long id;
    private String title;
    private String description;
    private Integer stepOrder;
    private String resourceUrl;
    private Integer estimatedDays;
    private String status;           // user's progress: NOT_STARTED, IN_PROGRESS, COMPLETED
}