package com.careercompass.backend.projectrecommendation.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String skillsCovered;

    @Column(nullable = false)
    private String difficulty;

    private Integer estimatedHours;

    @Column(nullable = false)
    private String relevantJobRole;
}