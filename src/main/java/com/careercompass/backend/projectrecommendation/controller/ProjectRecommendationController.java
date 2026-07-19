package com.careercompass.backend.projectrecommendation.controller;

import com.careercompass.backend.projectrecommendation.dto.ProjectResponse;
import com.careercompass.backend.projectrecommendation.service.ProjectRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectRecommendationController {

    private final ProjectRecommendationService projectRecommendationService;

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProjectResponse>> getRecommendations() {
        return ResponseEntity.ok(
                projectRecommendationService.getRecommendations());
    }
}