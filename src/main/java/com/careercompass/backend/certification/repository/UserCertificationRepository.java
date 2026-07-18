package com.careercompass.backend.certification.repository;

import com.careercompass.backend.certification.entity.UserCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCertificationRepository
        extends JpaRepository<UserCertification, Long> {

    List<UserCertification> findByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);
}