package com.careercompass.backend.skill.repository;

import com.careercompass.backend.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}
