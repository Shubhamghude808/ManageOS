package com.cafeos.agent;

import com.cafeos.pc.GamingPc;
import com.cafeos.pc.GamingPcRepository;
import com.cafeos.session.SessionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AgentService {
    private final GamingPcRepository pcs;
    private final PcHeartbeatRepository heartbeats;
    private final SessionService sessions;

    public AgentService(GamingPcRepository pcs, PcHeartbeatRepository heartbeats, SessionService sessions) {
        this.pcs = pcs; this.heartbeats = heartbeats; this.sessions = sessions;
    }

    @Transactional(readOnly = true)
    public java.util.Optional<SessionService.SessionDto> currentSession(UUID pcId) {
        return sessions.currentForPc(requirePcId(pcId));
    }

    @Transactional
    public AgentAcknowledgement register(UUID pcId, AgentRegistrationRequest request) {
        UUID authenticatedPcId = requirePcId(pcId);
        GamingPc pc = findPc(authenticatedPcId);
        Instant receivedAt = Instant.now();
        pc.registerAgent(request.hostname().trim());
        pc.recordHeartbeat(receivedAt, pcs.hasOpenSession(authenticatedPcId));
        heartbeats.save(new PcHeartbeat(pc, receivedAt, request.agentVersion(), null, null,
                Map.of("os", request.os(), "ipAddress", request.ipAddress())));
        return new AgentAcknowledgement(authenticatedPcId, "REGISTERED", receivedAt);
    }

    @Transactional
    public AgentAcknowledgement heartbeat(UUID pcId, AgentHeartbeatRequest request) {
        UUID authenticatedPcId = requirePcId(pcId);
        GamingPc pc = findPc(authenticatedPcId);
        Instant receivedAt = Instant.now();
        pc.recordHeartbeat(receivedAt, pcs.hasOpenSession(authenticatedPcId));
        heartbeats.save(new PcHeartbeat(pc, receivedAt, request.agentVersion(), request.cpuPercent(),
                request.memoryPercent(), Map.of("agentStatus", request.agentStatus())));
        return new AgentAcknowledgement(authenticatedPcId, "RECEIVED", receivedAt);
    }

    private GamingPc findPc(UUID pcId) {
        UUID requiredPcId = requirePcId(pcId);
        return pcs.findById(requiredPcId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown agent"));
    }

    private static UUID requirePcId(UUID pcId) {
        return Objects.requireNonNull(pcId, "Authenticated agent PC id is required");
    }

    public record AgentRegistrationRequest(String hostname, String os, String ipAddress, String agentVersion) {}
    public record AgentHeartbeatRequest(String agentStatus, String agentVersion, BigDecimal cpuPercent, BigDecimal memoryPercent) {}
    public record AgentAcknowledgement(UUID pcId, String status, Instant receivedAt) {}
}
