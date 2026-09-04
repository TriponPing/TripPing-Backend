package com.tripping.backend.home.service;

import com.tripping.backend.home.dto.response.PopularKeywordResponse;
import com.tripping.backend.home.repository.HomeRouteConditionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularKeywordService {

    private final HomeRouteConditionRepository routeConditionRepository;

    public List<PopularKeywordResponse> getPopularKeywords(int limit) {
        Pageable topN = PageRequest.of(0, limit);

        return routeConditionRepository.findPopularThemes(topN).stream()
                .map(projection -> PopularKeywordResponse.of(projection.getTheme(), projection.getCount()))
                .toList();
    }
}
