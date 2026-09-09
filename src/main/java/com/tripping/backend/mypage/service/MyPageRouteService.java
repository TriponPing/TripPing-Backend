package com.tripping.backend.mypage.service;

import com.tripping.backend.auth.repository.UserRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.ActualRouteSpot;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.entity.SavedRoute;
import com.tripping.backend.mypage.dto.PageResponse;
import com.tripping.backend.mypage.dto.SavedRouteResponse;
import com.tripping.backend.mypage.dto.SavedRouteSaveResponse;
import com.tripping.backend.mypage.repository.MyPageActualRouteRepository;
import com.tripping.backend.mypage.repository.MyPageActualRouteSpotRepository;
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

    private static final String UNKNOWN_NICKNAME = "알 수 없음";

    private final MyPageSavedRouteRepository savedRouteRepository;
    private final MyPageActualRouteRepository actualRouteRepository;
    private final MyPageActualRouteSpotRepository actualRouteSpotRepository;
    private final RepresentativeSpotFinder representativeSpotFinder;
    private final UserRepository userRepository;

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

        // 루트별 방문 장소 개수 ("핑 N개" 표시용) - 한 번에 조회해서 N+1 방지
        Map<Long, Long> placeCountByRoute = routeIds.isEmpty()
                ? Map.of()
                : actualRouteSpotRepository
                        .findByActualRouteIdInOrderByActualRouteIdAscVisitOrderAsc(routeIds).stream()
                        .collect(Collectors.groupingBy(ActualRouteSpot::getActualRouteId, Collectors.counting()));

        // 루트를 만든(다녀온) 계정의 닉네임 - 카드 제목으로 표시. 유저 id도 한 번에 모아서 N+1 방지
        List<Long> writerIds = routeById.values().stream().map(ActualRoute::getUserId).distinct().toList();
        Map<Long, String> nicknameByUserId = writerIds.isEmpty()
                ? Map.of()
                : userRepository.findAllById(writerIds).stream()
                        .collect(Collectors.toMap(AppUser::getUserId, AppUser::getNickname));

        List<SavedRouteResponse> content = page.getContent().stream()
                .map(sr -> {
                    ActualRoute route = routeById.get(sr.getActualRouteId());
                    RepresentativeSpotFinder.RepresentativeSpot rep = repByRoute.get(sr.getActualRouteId());
                    Long placeCount = placeCountByRoute.get(sr.getActualRouteId());
                    return new SavedRouteResponse(
                            sr.getSavedRouteId(),
                            sr.getActualRouteId(),
                            route != null ? route.getTravelDate() : null,
                            route != null ? route.getMemberCount() : null,
                            route != null ? nicknameByUserId.getOrDefault(route.getUserId(), UNKNOWN_NICKNAME) : UNKNOWN_NICKNAME,
                            rep != null ? rep.spotName() : null,
                            rep != null ? rep.imageUrl() : null,
                            placeCount != null ? placeCount.intValue() : 0,
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

    // 루트 저장(북마크) - POST /routes/{routeId}/saved
    // routeId = 북마크할 ACTUAL_ROUTE의 id (다른 사람이 공개해둔 다녀온 여행)
    @Transactional
    public SavedRouteSaveResponse saveRoute(Long userId, Long actualRouteId) {
        ActualRoute route = actualRouteRepository.findById(actualRouteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 루트입니다."));

        if (route.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인의 루트는 저장할 수 없습니다.");
        }

        boolean alreadySaved = savedRouteRepository.existsByUserIdAndActualRouteId(userId, actualRouteId);
        if (!alreadySaved) {
            SavedRoute savedRoute = SavedRoute.builder()
                    .userId(userId)
                    .actualRouteId(actualRouteId)
                    .build();
            savedRouteRepository.save(savedRoute);
        }

        return new SavedRouteSaveResponse(actualRouteId, true);
    }
}
