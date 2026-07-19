package com.careercompass.backend.certification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.careercompass.backend.user.entity.User;

import java.time.LocalDate;

@Entity
@Table(name = "user_certifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String certificationName;

    @Column(nullable = false)
    private String provider;

    private String url;

    private LocalDate completedAt;

    @Column(nullable = false)
    private String status;
}