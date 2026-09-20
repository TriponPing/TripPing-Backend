package com.tripping.backend.insight.repository;

import com.tripping.backend.insight.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 보고서 목록 카드는 최신순으로 보여줘야 하니 createdAt 내림차순 정렬해서 반환
    List<Report> findByOrganization_OrgIdOrderByCreatedAtDesc(Long orgId);

    // 상세 조회할 때 "이 보고서가 진짜 내 기관 것이 맞는지"까지 조건에 같이 넣어서 확인한다.
    // (다른 기관의 reportId를 URL에 직접 넣어서 남의 보고서를 열람하는 걸 막기 위함)
    Optional<Report> findByReportIdAndOrganization_OrgId(Long reportId, Long orgId);
}
