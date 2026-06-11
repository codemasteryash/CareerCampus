package com.careercompass.backend.certification.repository;

import com.careercompass.backend.certification.entity.UserCertification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCertificationRepository extends JpaRepository<UserCertification, Long> {
}
