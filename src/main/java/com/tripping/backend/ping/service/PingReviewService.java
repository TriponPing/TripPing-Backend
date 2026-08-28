package com.tripping.backend.ping.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.PingLog;
import com.tripping.backend.ping.dto.PingReviewRequest;
import com.tripping.backend.ping.dto.PingReviewResponse;
import com.tripping.backend.ping.repository.PingActualRouteRepository;
import com.tripping.backend.ping.repository.PingActualRouteSpotRepository;
import com.tripping.backend.ping.repository.PingLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        return toResponse(pingId, pingLogRepository.save(log));
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

    private PingReviewResponse toResponse(Long pingId, PingLog log) {
        return new PingReviewResponse(
                pingId,
                log.getRating(),
                log.getPhotoUrl(),
                log.getReviewComment(),
                log.getUpdatedAt()
        );
    }
}
