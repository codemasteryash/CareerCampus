package com.careercompass.backend.projectrecommendation.repository;

import com.careercompass.backend.projectrecommendation.entity.ProjectRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRecommendationRepository extends JpaRepository<ProjectRecommendation, Long> {
}
