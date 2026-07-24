package com.careercompass.backend.skill.repository;

import com.careercompass.backend.skill.entity.UserSkill;
import com.careercompass.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(User user);

    @Query("SELECT us FROM UserSkill us JOIN FETCH us.skill WHERE us.user.id = :userId")
    List<UserSkill> findByUserId(@Param("userId") Long userId);

    @Query("SELECT us FROM UserSkill us JOIN FETCH us.skill JOIN FETCH us.user WHERE us.user.id = :userId AND us.skill.id = :skillId")
    Optional<UserSkill> findByUserIdAndSkillId(
            @Param("userId") Long userId,
            @Param("skillId") Long skillId);

    boolean existsByUserIdAndSkillId(Long userId, Long skillId);
}
