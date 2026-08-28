package com.tripping.backend.mypage.dto;

import jakarta.validation.constraints.Size;

/**
 * 프로필 수정 요청 (PATCH /users/me)
 * 값이 null인 필드는 수정하지 않음 (partial update)
 */
public record ProfileUpdateRequest(

        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요.")
        String nickname,

        String profileImage,

        @Size(max = 3, message = "지역 코드 형식이 올바르지 않습니다.")
        String regionId,

        String language
) {
}
