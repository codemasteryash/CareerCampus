package com.careercompass.backend.roadmap.repository;

import com.careercompass.backend.roadmap.entity.UserRoadmapProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoadmapProgressRepository
        extends JpaRepository<UserRoadmapProgress, Long> {

    @Query("SELECT p FROM UserRoadmapProgress p " +
            "JOIN FETCH p.roadmapStep " +
            "JOIN FETCH p.roadmap " +
            "WHERE p.user.id = :userId AND p.roadmap.id = :roadmapId")
    List<UserRoadmapProgress> findByUserIdAndRoadmapId(
            @Param("userId") Long userId,
            @Param("roadmapId") Long roadmapId);

    @Query("SELECT p FROM UserRoadmapProgress p " +
            "JOIN FETCH p.roadmapStep " +
            "JOIN FETCH p.roadmap " +
            "WHERE p.user.id = :userId " +
            "AND p.roadmapStep.id = :roadmapStepId")
    Optional<UserRoadmapProgress> findByUserIdAndRoadmapStepId(
            @Param("userId") Long userId,
            @Param("roadmapStepId") Long roadmapStepId);

    long countByUserIdAndRoadmapIdAndStatus(
            Long userId, Long roadmapId, String status);
}