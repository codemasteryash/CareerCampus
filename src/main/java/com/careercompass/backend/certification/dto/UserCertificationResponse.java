package com.careercompass.backend.certification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCertificationResponse {
    private Long id;
    private Long certificationId;
    private String certificationName;
    private String provider;
    private String url;
    private String status;
    private LocalDate completedAt;
}