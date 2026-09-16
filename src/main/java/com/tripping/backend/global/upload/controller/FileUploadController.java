package com.tripping.backend.global.upload.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.global.upload.dto.UploadImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 이미지 업로드용 공용 컨트롤러 (Ping 후기 사진 등). 클라우드 스토리지가 아직 없어서 서버
 * 로컬 디스크(app.upload-dir)에 저장하고, 같은 컨트롤러의 GET으로 그대로 서빙함.
 * ⚠️ 로컬 디스크 저장이라 서버 재배포/재시작 시 파일이 사라질 수 있음 - 임시 구현.
 */
@Tag(name = "파일 업로드", description = "이미지 업로드/조회 API")
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif"
    );

    @Operation(summary = "이미지 업로드", description = "이미지 파일 하나를 업로드하고, 바로 조회 가능한 URL을 돌려줍니다.")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadImageResponse> uploadImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) throws IOException {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 크기는 10MB를 넘을 수 없습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일(jpg/png/webp/heic)만 업로드할 수 있습니다.");
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String filename = UUID.randomUUID() + extensionFor(contentType);
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파일명입니다.");
        }
        file.transferTo(target);

        // 클라이언트가 실제로 접속한 host:port 그대로 URL을 만들어서, 에뮬레이터(10.0.2.2)든
        // 실기기든 나중에 실서버 배포든 항상 그 요청과 같은 주소로 이미지 URL이 나가게 함.
        String url = UriComponentsBuilder.fromUriString(
                        request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort())
                .path("/uploads/")
                .path(filename)
                .toUriString();

        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadImageResponse(url));
    }

    @Operation(summary = "업로드된 이미지 조회", description = "업로드된 이미지 파일을 그대로 내려줍니다.")
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir) || !Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(target.toUri());
        String contentType = Files.probeContentType(target);
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/heic" -> ".heic";
            case "image/heif" -> ".heif";
            default -> ".jpg";
        };
    }
}