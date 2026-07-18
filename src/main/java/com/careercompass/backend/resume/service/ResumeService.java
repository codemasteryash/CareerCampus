package com.careercompass.backend.resume.service;

import com.careercompass.backend.resume.dto.ResumeAnalysisResponse;
import com.careercompass.backend.resume.dto.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    ResumeUploadResponse uploadResume(MultipartFile file);

    ResumeAnalysisResponse getResumeAnalysis();
}