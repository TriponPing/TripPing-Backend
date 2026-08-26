package com.tripping.backend.community.service;

import com.tripping.backend.community.dto.request.ReviewUpdateRequest;
import com.tripping.backend.community.dto.response.ReviewResponse;
import com.tripping.backend.community.repository.ActualRouteRepository;
import com.tripping.backend.community.repository.AppUserRepository;
import com.tripping.backend.community.repository.RouteCommentRepository;
import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.entity.AppUser;
import com.tripping.backend.entity.RouteComment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private static final String UNKNOWN_NICKNAME = "알 수 없음";

    private final RouteCommentRepository routeCommentRepository;
    private final ActualRouteRepository actualRouteRepository;
    private final AppUserRepository appUserRepository;

    public Page<ReviewResponse> getReviewsByRoute(Long routeId, Pageable pageable) {
        ActualRoute route = actualRouteRepository.findByActualRouteIdAndIsDeletedFalse(routeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 루트입니다. routeId=" + routeId));

        return routeCommentRepository.findByActualRouteIdAndIsDeletedFalse(route.getActualRouteId(), pageable)
                .map(comment -> ReviewResponse.from(comment, findNickname(comment.getUserId())));
    }

    @Transactional
    public ReviewResponse updateMyReview(Long currentUserId, Long reviewId, ReviewUpdateRequest request) {
        RouteComment comment = getOwnedComment(currentUserId, reviewId);
        comment.setContent(request.getContent());
        return ReviewResponse.from(comment, findNickname(comment.getUserId()));
    }

    @Transactional
    public void deleteMyReview(Long currentUserId, Long reviewId) {
        RouteComment comment = getOwnedComment(currentUserId, reviewId);
        comment.setIsDeleted(true);
    }

    private RouteComment getOwnedComment(Long currentUserId, Long reviewId) {
        RouteComment comment = routeCommentRepository.findById(reviewId)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 후기입니다. reviewId=" + reviewId));

        if (!comment.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 후기만 수정/삭제할 수 있습니다.");
        }

        return comment;
    }

    private String findNickname(Long userId) {
        return appUserRepository.findById(userId)
                .map(AppUser::getNickname)
                .orElse(UNKNOWN_NICKNAME);
    }
}
