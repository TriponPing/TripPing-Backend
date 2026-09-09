package com.tripping.backend.home.service;

import com.tripping.backend.home.dto.response.PopularKeywordResponse;
import com.tripping.backend.home.repository.HomePingLogTagRepository;
import java.time.LocalDateTime;
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

    private static final int WEEK_WINDOW_DAYS = 7;

    private final HomePingLogTagRepository pingLogTagRepository;

    // 이번 주(최근 7일) 작성된 Ping 후기에 달린 해시태그를 등장 빈도순으로 조회합니다.
    public List<PopularKeywordResponse> getPopularKeywords(int limit) {
        Pageable topN = PageRequest.of(0, limit);
        LocalDateTime since = LocalDateTime.now().minusDays(WEEK_WINDOW_DAYS);

        return pingLogTagRepository.findPopularTags(since, topN).stream()
                .map(projection -> PopularKeywordResponse.of(projection.getKeyword(), projection.getCount()))
                .toList();
    }
}
