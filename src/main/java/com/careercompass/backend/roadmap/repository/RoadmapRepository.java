package com.careercompass.backend.roadmap.repository;

import com.careercompass.backend.roadmap.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
}
