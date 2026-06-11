package com.careercompass.backend.certification.repository;

import com.careercompass.backend.certification.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
}
