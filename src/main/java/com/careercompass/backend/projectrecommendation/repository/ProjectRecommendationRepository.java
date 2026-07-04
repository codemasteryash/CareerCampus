package com.careercompass.backend.projectrecommendation.repository;

import com.careercompass.backend.projectrecommendation.entity.ProjectRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRecommendationRepository extends JpaRepository<ProjectRecommendation, Long> {

    List<ProjectRecommendation> findByRelevantJobRole(String relevantJobRole);

    List<ProjectRecommendation> findByRelevantJobRoleAndDifficulty(String relevantJobRole, String difficulty);
}