package com.tripping.backend.ping.repository; // 프로젝트 패키지 경로에 맞게 수정

import com.tripping.backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}