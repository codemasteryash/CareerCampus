package com.careercompass.backend.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String title;
    private String description;
    private String experienceLevel;
    private List<RoleSkillResponse> requiredSkills;
}