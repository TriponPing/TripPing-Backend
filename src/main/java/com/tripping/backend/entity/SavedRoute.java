package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_route")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SavedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_route_id")
    private Long savedRouteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "actual_route_id", nullable = false)
    private Long actualRouteId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}