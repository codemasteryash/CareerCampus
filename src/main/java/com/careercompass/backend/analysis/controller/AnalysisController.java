package com.careercompass.backend.analysis.controller;

import com.careercompass.backend.analysis.dto.AnalysisResponse;
import com.careercompass.backend.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping
    public ResponseEntity<AnalysisResponse> getAnalysis() {
        return ResponseEntity.ok(analysisService.getFullAnalysis());
    }
}