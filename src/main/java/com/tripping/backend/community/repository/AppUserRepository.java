package com.tripping.backend.community.repository;

import com.tripping.backend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 후기/루트 작성자의 닉네임을 표시하기 위한 조회용 인터페이스입니다.
 * 프로젝트에 이미 AppUser 용 Repository(예: auth 패키지 쪽)가 있다면 이 파일은 지우고
 * 그 Repository를 RouteService/ReviewService에 주입해서 쓰세요. (같은 엔티티에 여러 Repository
 * 인터페이스를 만들어도 동작에는 문제없지만, 굳이 중복 관리할 필요는 없습니다.)
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}
