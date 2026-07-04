package com.careercompass.backend.roadmap.repository;

import com.careercompass.backend.roadmap.entity.UserRoadmapProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoadmapProgressRepository extends JpaRepository<UserRoadmapProgress, Long> {

    List<UserRoadmapProgress> findByUserIdAndRoadmapId(Long userId, Long roadmapId);

    Optional<UserRoadmapProgress> findByUserIdAndRoadmapStepId(Long userId, Long roadmapStepId);

    long countByUserIdAndRoadmapIdAndStatus(Long userId, Long roadmapId, String status);
}