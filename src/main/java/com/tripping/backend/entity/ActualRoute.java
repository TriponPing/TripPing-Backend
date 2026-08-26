package com.tripping.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "actual_route")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ActualRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actual_route_id")
    private Long actualRouteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "companion_type", length = 30)
    private String companionType;

    @Column(length = 30)
    private String transport;

    @Column(name = "member_count")
    private Integer memberCount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RouteStatus status = RouteStatus.IN_PROGRESS; // 3-8 위젯: 여행 진행중 여부

    @Builder.Default
    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 소프트 삭제 여부 - TR-18

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