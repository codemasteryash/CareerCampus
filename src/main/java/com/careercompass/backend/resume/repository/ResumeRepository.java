package com.careercompass.backend.resume.repository;

import com.careercompass.backend.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
