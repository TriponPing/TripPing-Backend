package com.tripping.backend.ping.controller;

import com.tripping.backend.auth.service.CustomUserDetails;
import com.tripping.backend.ping.dto.RegionPingRequest;
import com.tripping.backend.ping.dto.RegionPingResponse;
import com.tripping.backend.ping.service.RegionPingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "RegionPing", description = "지역핑(내 거주 지역 장소 평점/후기) 등록 API")
@RestController
@RequiredArgsConstructor
public class RegionPingController {

    private final RegionPingService regionPingService;

    @Operation(summary = "지역핑 등록", description = "본인 거주 지역의 장소에 평점/후기를 남깁니다. (본인 거주 지역 장소가 아니면 403)")
    @PostMapping("/region-pings")
    public ResponseEntity<RegionPingResponse> registerRegionPing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegionPingRequest request
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        RegionPingResponse response = regionPingService.registerRegionPing(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
