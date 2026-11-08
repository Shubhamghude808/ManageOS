package com.cafeos.pc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

public interface GamingPcRepository extends JpaRepository<GamingPc, UUID> {
    List<GamingPc> findAllByBranchIdAndDeletedFalseOrderByNameAsc(UUID branchId);
    Optional<GamingPc> findByIdAndBranchId(UUID id, UUID branchId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pc from GamingPc pc where pc.id = :id")
    Optional<GamingPc> findByIdForUpdate(@Param("id") UUID id);
    boolean existsByBranchIdAndNameIgnoreCase(UUID branchId, String name);
    boolean existsByBranchIdAndNameIgnoreCaseAndDeletedFalse(UUID branchId, String name);
    Optional<GamingPc> findByAgentCredentialHashAndDeletedFalse(String agentCredentialHash);
    Optional<GamingPc> findByAgentMachineIdAndDeletedFalse(String agentMachineId);
    Optional<GamingPc> findByHostnameIgnoreCaseAndDeletedFalse(String hostname);

    @Modifying
    @Query(value = "UPDATE gaming_pcs SET status = 'OFFLINE' WHERE status IN ('AVAILABLE', 'OCCUPIED') AND last_heartbeat_at IS NOT NULL AND last_heartbeat_at < :cutoff", nativeQuery = true)
    int markStaleAgentsAsOffline(@Param("cutoff") java.time.Instant cutoff);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM gaming_sessions WHERE pc_id = :pcId AND status IN ('ACTIVE', 'PAUSED'))", nativeQuery = true)
    boolean hasOpenSession(@Param("pcId") UUID pcId);
}
