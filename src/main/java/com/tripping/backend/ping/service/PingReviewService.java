package com.tripping.backend.ping.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.PingLog;
import com.tripping.backend.entity.PingLogTag;
import com.tripping.backend.entity.Tag;
import com.tripping.backend.ping.dto.PingReviewRequest;
import com.tripping.backend.ping.dto.PingReviewResponse;
import com.tripping.backend.ping.repository.PingActualRouteRepository;
import com.tripping.backend.ping.repository.PingActualRouteSpotRepository;
import com.tripping.backend.ping.repository.PingLogRepository;
import com.tripping.backend.ping.repository.PingLogTagRepository;
import com.tripping.backend.ping.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Ping 후기(PING_LOG) CRUD.
 * pingId 파라미터는 ACTUAL_ROUTE_SPOT의 id입니다 - PING_LOG가 actual_route_spot_id로만
 * 연결되는 구조라서 그렇습니다 (자세한 설명은 PingReviewResponse 주석 참고).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PingReviewService {

    private final PingActualRouteSpotRepository actualRouteSpotRepository;
    private final PingActualRouteRepository actualRouteRepository;
    private final PingLogRepository pingLogRepository;
    private final TagRepository tagRepository;
    private final PingLogTagRepository pingLogTagRepository;

    // 👈 새로 추가: Ping 후기 조회 - GET /pings/{pingId}/review
    // 후기 작성 화면 진입 시 이미 등록된 후기가 있는지 확인해서 등록/수정 모드를 판단하는 데 씀.
    // 후기가 없으면 findActiveReview()가 404를 던지는데, 프론트에서는 이걸 "아직 후기 없음"으로 해석함.
    public PingReviewResponse getReview(Long userId, Long pingId) {
        findOwnedSpot(userId, pingId);
        PingLog log = findActiveReview(pingId);
        return toResponse(pingId, log);
    }

    // Ping 후기 등록 - POST /pings/{pingId}/review
    @Transactional
    public PingReviewResponse createReview(Long userId, Long pingId, PingReviewRequest request) {
        findOwnedSpot(userId, pingId);

        if (request.rating() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "평점은 필수입니다.");
        }
        if (pingLogRepository.findByActualRouteSpotIdAndIsDeletedFalse(pingId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 후기가 있습니다. 수정 API를 사용해주세요.");
        }

        PingLog log = PingLog.builder()
                .actualRouteSpotId(pingId)
                .rating(request.rating())
                .photoUrl(request.photoUrl())
                .reviewComment(request.reviewComment())
                .build();
        PingLog savedLog = pingLogRepository.save(log);

        // ⭐ 태그 저장 및 연결
        saveTagsForLog(savedLog, request.tags());

        return toResponse(pingId, savedLog);
    }

    // Ping 후기 수정 - PATCH /pings/{pingId}/review (null 필드는 그대로 둠)
    @Transactional
    public PingReviewResponse updateReview(Long userId, Long pingId, PingReviewRequest request) {
        findOwnedSpot(userId, pingId);
        PingLog log = findActiveReview(pingId);

        if (request.rating() != null) {
            log.setRating(request.rating());
        }
        if (request.photoUrl() != null) {
            log.setPhotoUrl(request.photoUrl());
        }
        if (request.reviewComment() != null) {
            log.setReviewComment(request.reviewComment());
        }

        // ⭐ 태그 수정 (요청에 태그가 포함되어 있다면 기존 연결을 끊고 새로 교체)
        if (request.tags() != null) {
            pingLogTagRepository.deleteByPingLog(log);
            saveTagsForLog(log, request.tags());
        }

        return toResponse(pingId, log);
    }

    // Ping 후기 삭제 - DELETE /pings/{pingId}/review (소프트 삭제)
    @Transactional
    public void deleteReview(Long userId, Long pingId) {
        findOwnedSpot(userId, pingId);
        PingLog log = findActiveReview(pingId);
        log.setIsDeleted(true);
    }

    private PingLog findActiveReview(Long pingId) {
        return pingLogRepository.findByActualRouteSpotIdAndIsDeletedFalse(pingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."));
    }

    // pingId(=actualRouteSpotId)가 실제로 존재하고, 그 방문 기록이 로그인한 유저의 여행인지 검증
    private ActualRouteSpot findOwnedSpot(Long userId, Long actualRouteSpotId) {
        ActualRouteSpot spot = actualRouteSpotRepository.findById(actualRouteSpotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "방문 기록을 찾을 수 없습니다."));

        ActualRoute route = actualRouteRepository.findById(spot.getActualRouteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행을 찾을 수 없습니다."));

        if (!route.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 여행 기록에만 후기를 남길 수 있습니다.");
        }
        return spot;
    }

    // 태그를 DB에 찾거나 생성한 뒤, PingLogTag 매핑 테이블에 저장하는 헬퍼 메서드
    private void saveTagsForLog(PingLog pingLog, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) continue;
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));

            PingLogTag pingLogTag = PingLogTag.builder()
                    .pingLog(pingLog)
                    .tag(tag)
                    .build();
            pingLogTagRepository.save(pingLogTag);
        }
    }

    private PingReviewResponse toResponse(Long pingId, PingLog log) {
        // 연결된 태그 목록을 조회해서 리스트로 변환
        List<String> tags = pingLogTagRepository.findByPingLog(log).stream()
                .map(pingLogTag -> pingLogTag.getTag().getName())
                .toList();

        return new PingReviewResponse(
                pingId,
                log.getRating(),
                log.getPhotoUrl(),
                log.getReviewComment(),
                tags,
                log.getUpdatedAt()
        );
    }
}