package com.careercompass.backend.certification.controller;

import com.careercompass.backend.certification.dto.CertificationResponse;
import com.careercompass.backend.certification.dto.EnrollCertificationRequest;
import com.careercompass.backend.certification.dto.UpdateCertificationStatusRequest;
import com.careercompass.backend.certification.dto.UserCertificationResponse;
import com.careercompass.backend.certification.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    @GetMapping
    public ResponseEntity<List<CertificationResponse>> getAllCertifications() {
        return ResponseEntity.ok(
                certificationService.getAllCertifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificationResponse> getCertificationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                certificationService.getCertificationById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserCertificationResponse>> getMyCertifications() {
        return ResponseEntity.ok(
                certificationService.getMyCertifications());
    }

    @PostMapping("/enroll")
    public ResponseEntity<UserCertificationResponse> enroll(
            @Valid @RequestBody EnrollCertificationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(certificationService.enrollInCertification(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserCertificationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCertificationStatusRequest request) {
        return ResponseEntity.ok(
                certificationService.updateCertificationStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCertification(
            @PathVariable Long id) {
        certificationService.removeCertification(id);
        return ResponseEntity.noContent().build();
    }
}