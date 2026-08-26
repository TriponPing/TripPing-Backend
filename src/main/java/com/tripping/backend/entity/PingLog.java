package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ping_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ping_log_id")
    private Long pingLogId;

    @Column(name = "actual_route_spot_id", nullable = false)
    private Long actualRouteSpotId;

    private Integer rating; // 1~5

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "review_comment", length = 2000)
    private String reviewComment;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 소프트 삭제 여부 - TR-21

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}