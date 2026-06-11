package com.careercompass.backend.skill.repository;

import com.careercompass.backend.skill.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
}
