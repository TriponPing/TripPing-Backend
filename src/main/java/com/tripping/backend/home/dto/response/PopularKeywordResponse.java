package com.tripping.backend.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "인기 키워드 응답")
public class PopularKeywordResponse {

    @Schema(description = "키워드(테마)", example = "감성")
    private String keyword;

    @Schema(description = "등장 횟수", example = "37")
    private Long count;

    public static PopularKeywordResponse of(String keyword, long count) {
        return PopularKeywordResponse.builder()
                .keyword(keyword)
                .count(count)
                .build();
    }
}
