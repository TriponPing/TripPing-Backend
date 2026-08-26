package com.tripping.backend.community.dto.response;

import com.tripping.backend.entity.RouteComment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "루트 후기 응답")
public class ReviewResponse {

    @Schema(description = "후기 ID", example = "10")
    private Long reviewId;

    @Schema(description = "루트 ID", example = "1")
    private Long routeId;

    @Schema(description = "작성자 ID", example = "5")
    private Long writerId;

    @Schema(description = "작성자 닉네임", example = "여행좋아")
    private String writerNickname;

    @Schema(description = "후기 내용", example = "풍경이 정말 좋았어요!")
    private String content;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    /**
     * RouteComment 엔티티는 작성자 닉네임을 갖고 있지 않아(user_id만 있음)
     * 별도로 조회한 닉네임을 파라미터로 받습니다.
     */
    public static ReviewResponse from(RouteComment comment, String writerNickname) {
        return ReviewResponse.builder()
                .reviewId(comment.getCommentId())
                .routeId(comment.getActualRouteId())
                .writerId(comment.getUserId())
                .writerNickname(writerNickname)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
