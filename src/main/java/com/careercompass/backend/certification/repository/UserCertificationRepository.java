package com.careercompass.backend.certification.repository;

import com.careercompass.backend.certification.entity.UserCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCertificationRepository extends JpaRepository<UserCertification, Long> {

    List<UserCertification> findByUserId(Long userId);

    Optional<UserCertification> findByUserIdAndCertificationId(Long userId, Long certificationId);

    boolean existsByUserIdAndCertificationId(Long userId, Long certificationId);

    long countByUserIdAndStatus(Long userId, String status);
}