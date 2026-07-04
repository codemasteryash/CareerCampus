package com.careercompass.backend.roadmap.repository;

import com.careercompass.backend.roadmap.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    Optional<Roadmap> findByJobRole(String jobRole);
}