package com.tripping.backend.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "후기 수정 요청")
public class ReviewUpdateRequest {

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 500, message = "후기는 500자 이내로 작성해주세요.")
    @Schema(description = "수정할 후기 내용", example = "다시 생각해보니 정말 최고의 여행이었어요!")
    private String content;
}
