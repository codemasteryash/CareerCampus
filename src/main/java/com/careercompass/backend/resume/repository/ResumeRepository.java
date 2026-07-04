package com.careercompass.backend.resume.repository;

import com.careercompass.backend.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByUserId(Long userId);
}