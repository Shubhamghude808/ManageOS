package com.cafeos.agent;

import com.cafeos.pc.GamingPc;
import com.cafeos.pc.GamingPcRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AgentService {
    private final GamingPcRepository pcs;
    private final PcHeartbeatRepository heartbeats;

    public AgentService(GamingPcRepository pcs, PcHeartbeatRepository heartbeats) {
        this.pcs = pcs; this.heartbeats = heartbeats;
    }

    @Transactional
    public AgentAcknowledgement register(UUID pcId, AgentRegistrationRequest request) {
        GamingPc pc = findPc(pcId);
        Instant receivedAt = Instant.now();
        pc.registerAgent(request.hostname().trim());
        pc.recordHeartbeat(receivedAt);
        heartbeats.save(new PcHeartbeat(pc, receivedAt, request.agentVersion(), null, null,
                Map.of("os", request.os(), "ipAddress", request.ipAddress())));
        return new AgentAcknowledgement(pcId, "REGISTERED", receivedAt);
    }

    @Transactional
    public AgentAcknowledgement heartbeat(UUID pcId, AgentHeartbeatRequest request) {
        GamingPc pc = findPc(pcId);
        Instant receivedAt = Instant.now();
        pc.recordHeartbeat(receivedAt);
        heartbeats.save(new PcHeartbeat(pc, receivedAt, request.agentVersion(), request.cpuPercent(),
                request.memoryPercent(), Map.of("agentStatus", request.agentStatus())));
        return new AgentAcknowledgement(pcId, "RECEIVED", receivedAt);
    }

    private GamingPc findPc(UUID pcId) {
        return pcs.findById(pcId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown agent"));
    }

    public record AgentRegistrationRequest(String hostname, String os, String ipAddress, String agentVersion) {}
    public record AgentHeartbeatRequest(String agentStatus, String agentVersion, BigDecimal cpuPercent, BigDecimal memoryPercent) {}
    public record AgentAcknowledgement(UUID pcId, String status, Instant receivedAt) {}
}
