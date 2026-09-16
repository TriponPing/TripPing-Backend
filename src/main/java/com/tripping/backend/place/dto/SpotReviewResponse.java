package com.tripping.backend.place.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SpotReviewResponse {
    private String writerNickname;
    private Integer rating;
    private String reviewComment;
    private String photoUrl;
    private LocalDateTime createdAt;

    public SpotReviewResponse(
            String writerNickname,
            Integer rating,
            String reviewComment,
            String photoUrl,
            LocalDateTime createdAt
    ) {
        this.writerNickname = writerNickname;
        this.rating = rating;
        this.reviewComment = reviewComment;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
    }
}
