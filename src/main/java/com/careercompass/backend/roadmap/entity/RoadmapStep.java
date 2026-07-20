package com.careercompass.backend.roadmap.entity;
import com.careercompass.backend.roadmap.entity.Roadmap;
import lombok.Setter;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roadmap_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer stepOrder;

    private String resourceUrl;
    private Integer estimatedDays;
}
