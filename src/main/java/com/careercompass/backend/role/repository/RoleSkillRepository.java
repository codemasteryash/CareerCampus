package com.careercompass.backend.role.repository;

import com.careercompass.backend.role.entity.RoleSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleSkillRepository extends JpaRepository<RoleSkill, Long> {

    List<RoleSkill> findByJobRoleId(Long jobRoleId);

    List<RoleSkill> findByJobRoleIdAndImportance(Long jobRoleId, String importance);
}