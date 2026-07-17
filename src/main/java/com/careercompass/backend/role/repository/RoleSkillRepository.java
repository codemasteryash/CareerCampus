package com.careercompass.backend.role.repository;

import com.careercompass.backend.role.entity.RoleSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleSkillRepository extends JpaRepository<RoleSkill, Long> {

    @Query("SELECT rs FROM RoleSkill rs JOIN FETCH rs.skill WHERE rs.jobRole.id = :jobRoleId")
    List<RoleSkill> findByJobRoleId(@Param("jobRoleId") Long jobRoleId);

    @Query("SELECT rs FROM RoleSkill rs JOIN FETCH rs.skill WHERE rs.jobRole.id = :jobRoleId AND rs.importance = :importance")
    List<RoleSkill> findByJobRoleIdAndImportance(
            @Param("jobRoleId") Long jobRoleId,
            @Param("importance") String importance);
}