package com.careercompass.backend.roadmap.repository;

import com.careercompass.backend.roadmap.entity.RoadmapStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {
}
