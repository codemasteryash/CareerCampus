package com.careercompass.backend.role.service;

import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.role.dto.RoleResponse;
import com.careercompass.backend.role.dto.RoleSkillResponse;
import com.careercompass.backend.role.entity.JobRole;
import com.careercompass.backend.role.entity.RoleSkill;
import com.careercompass.backend.role.repository.JobRoleRepository;
import com.careercompass.backend.role.repository.RoleSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final JobRoleRepository jobRoleRepository;
    private final RoleSkillRepository roleSkillRepository;

    @Override
    public List<RoleResponse> getAllRoles() {
        return jobRoleRepository.findAll()
                .stream()
                .map(this::mapToRoleResponse)
                .toList();
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        JobRole jobRole = jobRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job role not found with id: " + id));
        return mapToRoleResponse(jobRole);
    }

    @Override
    public RoleResponse getRoleByTitle(String title) {
        JobRole jobRole = jobRoleRepository.findByTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job role not found with title: " + title));
        return mapToRoleResponse(jobRole);
    }

    private RoleResponse mapToRoleResponse(JobRole jobRole) {
        // Load all required skills for this role from RoleSkill table
        // and map each to a nested RoleSkillResponse
        List<RoleSkillResponse> requiredSkills =
                roleSkillRepository.findByJobRoleId(jobRole.getId())
                        .stream()
                        .map(this::mapToRoleSkillResponse)
                        .toList();

        return RoleResponse.builder()
                .id(jobRole.getId())
                .title(jobRole.getTitle())
                .description(jobRole.getDescription())
                .experienceLevel(jobRole.getExperienceLevel())
                .requiredSkills(requiredSkills)
                .build();
    }

    private RoleSkillResponse mapToRoleSkillResponse(RoleSkill roleSkill) {
        return RoleSkillResponse.builder()
                .skillId(roleSkill.getSkill().getId())
                .skillName(roleSkill.getSkill().getName())
                .importance(roleSkill.getImportance())
                .build();
    }
}