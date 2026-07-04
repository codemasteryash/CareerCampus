package com.careercompass.backend.role.repository;

import com.careercompass.backend.role.entity.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRoleRepository extends JpaRepository<JobRole, Long> {

    Optional<JobRole> findByTitle(String title);

    boolean existsByTitle(String title);
}