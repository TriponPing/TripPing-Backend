package com.tripping.backend.mypage.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.SavedRoute;
import com.tripping.backend.mypage.dto.PageResponse;
import com.tripping.backend.mypage.dto.SavedRouteResponse;
import com.tripping.backend.mypage.repository.MyPageActualRouteRepository;
import com.tripping.backend.mypage.repository.MyPageSavedRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageRouteService {

    private final MyPageSavedRouteRepository savedRouteRepository;
    private final MyPageActualRouteRepository actualRouteRepository;
    private final RepresentativeSpotFinder representativeSpotFinder;

    // 저장한 루트 목록 조회 - GET /users/me/routes/saved
    public PageResponse<SavedRouteResponse> getSavedRoutes(Long userId, Pageable pageable) {
        Page<SavedRoute> page = savedRouteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Long> routeIds = page.getContent().stream()
                .map(SavedRoute::getActualRouteId)
                .toList();

        Map<Long, ActualRoute> routeById = routeIds.isEmpty()
                ? Map.of()
                : actualRouteRepository.findAllById(routeIds).stream()
                        .collect(Collectors.toMap(ActualRoute::getActualRouteId, r -> r));
        Map<Long, RepresentativeSpotFinder.RepresentativeSpot> repByRoute = representativeSpotFinder.find(routeIds);

        List<SavedRouteResponse> content = page.getContent().stream()
                .map(sr -> {
                    ActualRoute route = routeById.get(sr.getActualRouteId());
                    RepresentativeSpotFinder.RepresentativeSpot rep = repByRoute.get(sr.getActualRouteId());
                    return new SavedRouteResponse(
                            sr.getSavedRouteId(),
                            sr.getActualRouteId(),
                            route != null ? route.getTravelDate() : null,
                            route != null ? route.getMemberCount() : null,
                            rep != null ? rep.spotName() : null,
                            rep != null ? rep.imageUrl() : null,
                            sr.getCreatedAt()
                    );
                })
                .toList();

        return PageResponse.of(content, page);
    }

    // 루트 저장 취소(북마크 해제) - DELETE /routes/{routeId}/saved
    // routeId = 북마크가 참조하는 ACTUAL_ROUTE의 id
    @Transactional
    public void unsaveRoute(Long userId, Long actualRouteId) {
        SavedRoute savedRoute = savedRouteRepository
                .findByUserIdAndActualRouteId(userId, actualRouteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "저장된 루트를 찾을 수 없습니다."));
        savedRouteRepository.delete(savedRoute);
    }
}
