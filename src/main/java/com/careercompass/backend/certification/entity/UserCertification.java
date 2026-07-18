package com.careercompass.backend.certification.entity;

import com.careercompass.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_certifications")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
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
    private String status; // IN_PROGRESS, COMPLETED
}