package com.tripping.backend.insight.repository;

import java.time.LocalDate;

// countVisitsByDay() 네이티브 쿼리 결과를 매핑하는 프로젝션. 컬럼 별칭(visitDate, visitCount)과
// getter 이름이 일치해야 스프링 데이터가 자동으로 값을 채워준다.
public interface DailyVisitProjection {
    LocalDate getVisitDate();
    Long getVisitCount();
}
