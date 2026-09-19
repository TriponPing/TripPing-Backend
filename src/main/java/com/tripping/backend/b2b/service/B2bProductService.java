package com.tripping.backend.b2b.service;

import com.tripping.backend.b2b.dto.*;
import com.tripping.backend.b2b.repository.*;
import com.tripping.backend.community.repository.RegionRepository;
import com.tripping.backend.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// 기관이 기획하는 관광상품(tour_product) 관리.
//
// 상품은 기관 단위로 격리된다. 모든 조회·수정은 로그인한 기관의 orgId를
// 조건에 함께 넣어, 다른 기관 상품에 접근할 수 없게 한다.
@RequiredArgsConstructor
@Service
public class B2bProductService {

    // 화면에서 고르는 진행 상태. tour_product.status는 문자열이라
    // 아무 값이나 들어갈 수 있어서 여기서 막는다.
    private static final Set<String> ALLOWED_STATUS =
            Set.of("DRAFT", "REVIEW", "DONE", "PUBLISHED");

    private static final String DEFAULT_PRODUCT_NAME = "새 관광상품 초안";

    private final TourProductRepository productRepository;
    private final TourProductSpotRepository productSpotRepository;
    private final ProductHashtagRepository productHashtagRepository;
    private final B2bTouristSpotRepository touristSpotRepository;
    private final B2bHashtagRepository hashtagRepository;
    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> list(Long orgId) {
        List<TourProduct> products =
                productRepository.findByOrgIdAndIsDeletedFalseOrderByUpdatedAtDesc(orgId);
        return products.stream()
                .map(product -> ProductSummaryResponse.of(
                        product,
                        regionName(product.getRegionId()),
                        productSpotRepository
                                .findByProductIdOrderByVisitOrderAsc(product.getProductId()).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse detail(Long orgId, Long productId) {
        TourProduct product = mine(orgId, productId);
        return ProductDetailResponse.of(
                product, regionName(product.getRegionId()), spotsOf(productId), hashtagsOf(productId));
    }

    @Transactional
    public ProductDetailResponse create(Long orgId, ProductCreateRequest request) {
        String name = (request == null || isBlank(request.getProductName()))
                ? DEFAULT_PRODUCT_NAME
                : request.getProductName().trim();

        TourProduct product = TourProduct.builder()
                .orgId(orgId)
                .productName(name)
                .regionId(request == null ? null : emptyToNull(request.getRegionId()))
                .status("DRAFT")
                .build();
        productRepository.save(product);

        return ProductDetailResponse.of(
                product, regionName(product.getRegionId()), List.of(), List.of());
    }

    @Transactional
    public ProductDetailResponse update(Long orgId, Long productId, ProductUpdateRequest request) {
        TourProduct product = mine(orgId, productId);

        if (!isBlank(request.getProductName())) {
            product.setProductName(request.getProductName().trim());
        }
        if (request.getStatus() != null) product.setStatus(validStatus(request.getStatus()));
        if (request.getRegionId() != null) product.setRegionId(emptyToNull(request.getRegionId()));
        if (request.getTargetCustomer() != null) product.setTargetCustomer(request.getTargetCustomer());
        if (request.getExpectedDuration() != null) {
            product.setExpectedDuration(request.getExpectedDuration());
        }
        if (request.getTransport() != null) product.setTransport(request.getTransport());
        if (request.getMealIncluded() != null) product.setMealIncluded(request.getMealIncluded());
        if (request.getTrendBasis() != null) product.setTrendBasis(request.getTrendBasis());
        if (request.getCautionNotes() != null) product.setCautionNotes(request.getCautionNotes());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());

        if (request.getSpots() != null) replaceSpots(productId, request.getSpots());
        if (request.getHashtags() != null) replaceHashtags(productId, request.getHashtags());

        productRepository.save(product);
        return ProductDetailResponse.of(
                product, regionName(product.getRegionId()), spotsOf(productId), hashtagsOf(productId));
    }

    // 실제로 지우지 않고 is_deleted만 세운다 (BS-17).
    @Transactional
    public void delete(Long orgId, Long productId) {
        TourProduct product = mine(orgId, productId);
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    private TourProduct mine(Long orgId, Long productId) {
        return productRepository.findByProductIdAndOrgIdAndIsDeletedFalse(productId, orgId)
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
    }

    // 일정은 순서가 핵심이라 부분 수정 대신 통째로 갈아끼운다. 보내온 배열의
    // 순서가 그대로 visit_order가 된다.
    private void replaceSpots(Long productId, List<ProductSpotRequest> spots) {
        productSpotRepository.deleteByProductId(productId);
        int order = 1;
        for (ProductSpotRequest spot : spots) {
            if (spot.getSpotId() == null) continue;
            productSpotRepository.save(TourProductSpot.builder()
                    .productId(productId)
                    .spotId(spot.getSpotId())
                    .visitOrder(order++)
                    .stayDuration(spot.getStayDuration())
                    .build());
        }
    }

    // 해시태그는 이름으로 받아, 없으면 hashtag 테이블에 새로 만들어 연결한다.
    private void replaceHashtags(Long productId, List<String> names) {
        productHashtagRepository.deleteByProductId(productId);
        Set<String> seen = new LinkedHashSet<>();
        for (String raw : names) {
            if (isBlank(raw)) continue;
            String name = raw.trim();
            if (name.startsWith("#")) name = name.substring(1);
            if (name.isEmpty() || !seen.add(name)) continue;
            String tagName = name;
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() -> hashtagRepository.save(
                            Hashtag.builder().name(tagName).build()));
            productHashtagRepository.save(ProductHashtag.builder()
                    .productId(productId)
                    .hashtagId(hashtag.getHashtagId())
                    .build());
        }
    }

    private List<ProductSpotResponse> spotsOf(Long productId) {
        List<TourProductSpot> rows =
                productSpotRepository.findByProductIdOrderByVisitOrderAsc(productId);
        if (rows.isEmpty()) return List.of();

        List<Long> spotIds = rows.stream().map(TourProductSpot::getSpotId).toList();
        Map<Long, TouristSpot> spots = touristSpotRepository.findAllById(spotIds).stream()
                .collect(Collectors.toMap(TouristSpot::getSpotId, Function.identity()));

        return rows.stream()
                .map(row -> {
                    TouristSpot spot = spots.get(row.getSpotId());
                    return ProductSpotResponse.builder()
                            .spotId(row.getSpotId())
                            .name(spot == null ? null : spot.getName())
                            .address(spot == null ? null : spot.getAddress())
                            .latitude(spot == null ? null : spot.getLatitude())
                            .longitude(spot == null ? null : spot.getLongitude())
                            .visitOrder(row.getVisitOrder())
                            .stayDuration(row.getStayDuration())
                            .build();
                })
                .toList();
    }

    private List<String> hashtagsOf(Long productId) {
        List<Long> ids = productHashtagRepository.findByProductId(productId).stream()
                .map(ProductHashtag::getHashtagId).toList();
        if (ids.isEmpty()) return List.of();
        return hashtagRepository.findAllById(ids).stream().map(Hashtag::getName).toList();
    }

    private String regionName(String regionId) {
        if (isBlank(regionId)) return null;
        return regionRepository.findById(regionId).map(Region::getRegionName).orElse(null);
    }

    private String validStatus(String status) {
        String upper = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUS.contains(upper)) {
            throw new IllegalArgumentException("알 수 없는 상태입니다: " + status);
        }
        return upper;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
