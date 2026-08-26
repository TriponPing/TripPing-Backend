package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hashtag")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long hashtagId;

    @Column(nullable = false, length = 50)
    private String name;
}