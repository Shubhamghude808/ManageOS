package com.cafeos.session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GamingSessionRepository extends JpaRepository<GamingSession, UUID> {
    List<GamingSession> findAllByBranchIdOrderByStartedAtDesc(UUID branchId);
    List<GamingSession> findAllByStatus(SessionStatus status);
    boolean existsByPcId(UUID pcId);

    @Query("select session from GamingSession session where session.pc.id = :pcId and session.status in (com.cafeos.session.SessionStatus.ACTIVE, com.cafeos.session.SessionStatus.PAUSED) order by session.startedAt desc")
    Optional<GamingSession> findOpenByPcId(@Param("pcId") UUID pcId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from GamingSession session where session.id = :id")
    Optional<GamingSession> findByIdForUpdate(@Param("id") UUID id);
}
