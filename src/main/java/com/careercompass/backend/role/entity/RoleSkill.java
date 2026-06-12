package com.careercompass.backend.role.entity;


import com.careercompass.backend.skill.entity.Skill;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_role_id", "skill_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_role_id", nullable = false)
    private JobRole jobRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private String importance;
}