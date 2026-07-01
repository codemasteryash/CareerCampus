package com.careercompass.backend.skill.repository;

import com.careercompass.backend.skill.entity.UserSkill;
import com.careercompass.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(User user);

    List<UserSkill> findByUserId(Long userId);

    Optional<UserSkill> findByUserIdAndSkillId(Long userId, Long skillId);



    boolean existsByUserIdAndSkillId(Long userId, Long skillId);
}