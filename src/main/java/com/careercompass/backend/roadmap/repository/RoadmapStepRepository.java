package com.careercompass.backend.roadmap.repository;

import com.careercompass.backend.roadmap.entity.RoadmapStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {

    List<RoadmapStep> findByRoadmapIdOrderByStepOrderAsc(Long roadmapId);
}