package com.tripping.backend.mypage.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 목록 API 공용 페이징 응답
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
