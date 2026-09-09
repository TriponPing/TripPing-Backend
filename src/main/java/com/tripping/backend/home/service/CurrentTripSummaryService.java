package com.tripping.backend.home.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.home.dto.response.CurrentTripSummaryResponse;
import com.tripping.backend.home.repository.HomeCurrentTripRepository;
import com.tripping.backend.home.repository.HomeWidgetPingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentTripSummaryService {

    private final HomeCurrentTripRepository currentTripRepository;
    private final HomeWidgetPingRepository widgetPingRepository;

    public CurrentTripSummaryResponse getCurrentTripSummary(Long userId) {
        // 정상적으로는 유저당 IN_PROGRESS 여행이 1개여야 하지만, 테스트 중 완료 처리를 안 하고
        // 여러 번 시작해서 2개 이상 쌓인 경우도 있어 Pageable로 최신 1개만 안전하게 가져옴.
        List<ActualRoute> routes = currentTripRepository.findInProgressRoutes(userId, PageRequest.of(0, 1));
        if (routes.isEmpty()) {
            return null;
        }
        ActualRoute route = routes.get(0);

        long pingCount = widgetPingRepository.countByActualRouteIdAndIsDeletedFalse(route.getActualRouteId());
        List<String> visitedPlaceNames = widgetPingRepository.findVisitedPlaceNames(route.getActualRouteId());

        return CurrentTripSummaryResponse.of(route, pingCount, visitedPlaceNames);
    }
}
