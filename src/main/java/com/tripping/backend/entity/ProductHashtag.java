package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_hashtag")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "hashtag_id", nullable = false)
    private Long hashtagId;
}