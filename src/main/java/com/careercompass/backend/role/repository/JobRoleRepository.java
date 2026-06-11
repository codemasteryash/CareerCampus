package com.careercompass.backend.role.repository;

import com.careercompass.backend.role.entity.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
}
