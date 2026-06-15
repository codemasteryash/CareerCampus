package com.careercompass.backend.skill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillResponse {
    private Long skillId;
    private String skillName;
    private String category;
    private String proficiencyLevel;
    private String source;
}
