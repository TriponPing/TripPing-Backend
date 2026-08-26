package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badge")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "badge_name", nullable = false, length = 50)
    private String badgeName;

    @Column(name = "condition_desc", length = 255)
    private String conditionDesc;
}