package com.tripping.backend.place.dto;

import lombok.Getter;

@Getter
public class SpotReviewResponse {
    private String writerNickname;
    private Integer rating;
    private String reviewComment;

    public SpotReviewResponse(String writerNickname, Integer rating, String reviewComment) {
        this.writerNickname = writerNickname;
        this.rating = rating;
        this.reviewComment = reviewComment;
    }
}