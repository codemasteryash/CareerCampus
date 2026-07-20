package com.careercompass.backend.roadmap.repository;

import com.careercompass.backend.roadmap.entity.RoadmapStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoadmapStepRepository
        extends JpaRepository<RoadmapStep, Long> {

    @Query("SELECT rs FROM RoadmapStep rs " +
            "JOIN FETCH rs.roadmap " +
            "WHERE rs.roadmap.id = :roadmapId " +
            "ORDER BY rs.stepOrder ASC")
    List<RoadmapStep> findByRoadmapIdOrderByStepOrderAsc(
            @Param("roadmapId") Long roadmapId);

    @Query("SELECT rs FROM RoadmapStep rs " +
            "JOIN FETCH rs.roadmap " +
            "WHERE rs.id = :id")
    Optional<RoadmapStep> findByIdWithRoadmap(
            @Param("id") Long id);
}