package com.careercompass.backend.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotBlank(message = "Proficiency level is required")
    private String proficiencyLevel;
}