package com.careercompass.backend.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSkillResponse {
    private Long skillId;
    private String skillName;
    private String importance;   // MUST_HAVE, NICE_TO_HAVE
}

