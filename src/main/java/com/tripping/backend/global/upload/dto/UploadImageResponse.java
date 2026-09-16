package com.tripping.backend.global.upload.dto;

/** 이미지 업로드 응답. 업로드된 파일을 그대로 읽어올 수 있는 URL 하나만 돌려줌. */
public record UploadImageResponse(String url) {
}
