package com.careercompass.backend.roadmap.controller;

import com.careercompass.backend.roadmap.dto.ProgressResponse;
import com.careercompass.backend.roadmap.dto.RoadmapResponse;
import com.careercompass.backend.roadmap.dto.UpdateProgressRequest;
import com.careercompass.backend.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @GetMapping
    public ResponseEntity<RoadmapResponse> getMyRoadmap() {
        return ResponseEntity.ok(roadmapService.getMyRoadmap());
    }

    @PatchMapping("/progress")
    public ResponseEntity<ProgressResponse> updateProgress(
            @Valid @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(
                roadmapService.updateStepProgress(request));
    }
}