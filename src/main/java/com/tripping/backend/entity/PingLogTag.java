package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ping_log_tag")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PingLogTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ping_log_tag_id")
    private Long pingLogTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ping_log_id", nullable = false)
    private PingLog pingLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}