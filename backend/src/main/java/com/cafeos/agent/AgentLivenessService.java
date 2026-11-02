package com.cafeos.agent;

import com.cafeos.pc.GamingPcRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentLivenessService {
    private final GamingPcRepository pcs;
    public AgentLivenessService(GamingPcRepository pcs) { this.pcs = pcs; }

    @Scheduled(fixedDelay = 15_000)
    @Transactional
    public void markStaleAgentsOffline() {
        pcs.markStaleAvailableAsOffline(Instant.now().minusSeconds(45));
    }
}
