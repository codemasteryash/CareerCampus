package com.careercompass.backend.certification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCertificationStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;          // IN_PROGRESS, COMPLETED

    private LocalDate completedAt;  // required only when status = COMPLETED
}