package com.careercompass.backend.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapResponse {
    private String targetJobRole;
    private List<String> presentSkills;
    private List<String> missingMustHaveSkills;
    private List<String> missingNiceToHaveSkills;
}