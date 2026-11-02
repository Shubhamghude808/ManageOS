package com.cafeos.agent;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcHeartbeatRepository extends JpaRepository<PcHeartbeat, UUID> {}
