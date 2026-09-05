package com.tripping.backend.home.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.home.dto.response.CurrentTripSummaryResponse;
import com.tripping.backend.home.repository.HomeCurrentTripRepository;
import com.tripping.backend.home.repository.HomeWidgetPingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentTripSummaryService {

    private final HomeCurrentTripRepository currentTripRepository;
    private final HomeWidgetPingRepository widgetPingRepository;

    public CurrentTripSummaryResponse getCurrentTripSummary(Long userId) {
        ActualRoute route = currentTripRepository.findFirstInProgressRoute(userId).orElse(null);
        if (route == null) {
            return null;
        }

        long pingCount = widgetPingRepository.countByActualRouteIdAndIsDeletedFalse(route.getActualRouteId());
        List<String> visitedPlaceNames = widgetPingRepository.findVisitedPlaceNames(route.getActualRouteId());

        return CurrentTripSummaryResponse.of(route, pingCount, visitedPlaceNames);
    }
}
