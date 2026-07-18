package com.careercompass.backend.certification.repository;

import com.careercompass.backend.certification.entity.UserCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCertificationRepository
        extends JpaRepository<UserCertification, Long> {

    @Query("SELECT uc FROM UserCertification uc " +
            "JOIN FETCH uc.certification " +
            "JOIN FETCH uc.user " +
            "WHERE uc.user.id = :userId")
    List<UserCertification> findByUserId(@Param("userId") Long userId);

    @Query("SELECT uc FROM UserCertification uc " +
            "WHERE uc.user.id = :userId " +
            "AND uc.certification.id = :certificationId")
    Optional<UserCertification> findByUserIdAndCertificationId(
            @Param("userId") Long userId,
            @Param("certificationId") Long certificationId);

    boolean existsByUserIdAndCertificationId(
            Long userId, Long certificationId);

    long countByUserIdAndStatus(Long userId, String status);
}