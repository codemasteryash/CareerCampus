package com.careercompass.backend.resume.controller;

import com.careercompass.backend.resume.dto.ResumeAnalysisResponse;
import com.careercompass.backend.resume.dto.ResumeUploadResponse;
import com.careercompass.backend.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resumeService.uploadResume(file));
    }

    @GetMapping("/analysis")
    public ResponseEntity<ResumeAnalysisResponse> getResumeAnalysis() {
        return ResponseEntity.ok(resumeService.getResumeAnalysis());
    }
}