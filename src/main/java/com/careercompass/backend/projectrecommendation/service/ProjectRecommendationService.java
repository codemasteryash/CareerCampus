package com.careercompass.backend.projectrecommendation.service;

import com.careercompass.backend.projectrecommendation.dto.ProjectResponse;

import java.util.List;

public interface ProjectRecommendationService {

    List<ProjectResponse> getRecommendations();
}