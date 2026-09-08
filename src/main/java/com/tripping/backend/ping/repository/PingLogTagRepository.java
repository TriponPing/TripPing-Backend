package com.tripping.backend.ping.repository;

import com.tripping.backend.entity.PingLog;
import com.tripping.backend.entity.PingLogTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PingLogTagRepository extends JpaRepository<PingLogTag, Long> {
    List<PingLogTag> findByPingLog(PingLog pingLog);
    void deleteByPingLog(PingLog pingLog);
}