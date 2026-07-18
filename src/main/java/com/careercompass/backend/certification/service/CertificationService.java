package com.careercompass.backend.certification.service;

import com.careercompass.backend.certification.dto.CertificationResponse;
import com.careercompass.backend.certification.dto.EnrollCertificationRequest;
import com.careercompass.backend.certification.dto.UpdateCertificationStatusRequest;
import com.careercompass.backend.certification.dto.UserCertificationResponse;

import java.util.List;

public interface CertificationService {


    List<CertificationResponse> getAllCertifications();
    CertificationResponse getCertificationById(Long id);

    List<UserCertificationResponse> getMyCertifications();
    UserCertificationResponse enrollInCertification(EnrollCertificationRequest request);
    UserCertificationResponse updateCertificationStatus(Long userCertificationId,
                                                        UpdateCertificationStatusRequest request);
    void removeCertification(Long userCertificationId);
}