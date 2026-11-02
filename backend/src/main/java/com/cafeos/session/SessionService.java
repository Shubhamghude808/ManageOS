package com.cafeos.session;

import com.cafeos.branch.Branch;
import com.cafeos.branch.BranchRepository;
import com.cafeos.pc.GamingPc;
import com.cafeos.pc.GamingPcRepository;
import com.cafeos.pc.PcStatus;
import com.cafeos.pricing.PricingPlan;
import com.cafeos.pricing.PricingPlanRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SessionService {
    private final GamingSessionRepository sessions;
    private final GamingPcRepository pcs;
    private final PricingPlanRepository plans;
    private final BranchRepository branches;

    public SessionService(GamingSessionRepository sessions, GamingPcRepository pcs,
                          PricingPlanRepository plans, BranchRepository branches) {
        this.sessions = sessions; this.pcs = pcs; this.plans = plans; this.branches = branches;
    }

    @Transactional(readOnly = true)
    public List<SessionDto> list(UUID branchId) {
        requireBranch(branchId);
        Instant now = Instant.now();
        return sessions.findAllByBranchIdOrderByStartedAtDesc(branchId).stream().map(s -> toDto(s, now)).toList();
    }

    @Transactional
    public SessionDto start(StartSessionRequest request) {
        GamingPc pc = pcs.findByIdForUpdate(request.pcId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PC not found"));
        if (pc.getStatus() != PcStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PC is not available for a session");
        }
        PricingPlan plan = plans.findByIdAndBranchIdAndActiveTrue(request.pricingPlanId(), pc.getBranch().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active pricing plan not found for this branch"));
        GamingSession session = sessions.save(new GamingSession(pc.getBranch(), pc, plan, Instant.now(), request.allocatedSeconds()));
        pc.markOccupied();
        return toDto(session, Instant.now());
    }

    @Transactional
    public SessionDto pause(UUID id) {
        GamingSession session = requireSessionForUpdate(id);
        Instant now = Instant.now();
        if (session.getStatus() == SessionStatus.ACTIVE && !now.isBefore(session.expiryAt())) {
            completeAtExpiry(session);
            return toDto(session, now);
        }
        requireStatus(session, SessionStatus.ACTIVE);
        session.pause(now);
        return toDto(session, now);
    }

    @Transactional
    public SessionDto resume(UUID id) {
        GamingSession session = requireSessionForUpdate(id);
        requireStatus(session, SessionStatus.PAUSED);
        session.resume(Instant.now());
        return toDto(session, Instant.now());
    }

    @Transactional
    public SessionDto extend(UUID id, long addedSeconds) {
        GamingSession session = requireSessionForUpdate(id);
        Instant now = Instant.now();
        if (session.getStatus() == SessionStatus.ACTIVE && !now.isBefore(session.expiryAt())) {
            completeAtExpiry(session);
            return toDto(session, now);
        }
        if (session.getStatus() != SessionStatus.ACTIVE && session.getStatus() != SessionStatus.PAUSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only an open session can be extended");
        }
        if (addedSeconds > 86_400 - session.getAllocatedSeconds()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocated session time cannot exceed 24 hours");
        }
        session.extend(addedSeconds);
        return toDto(session, now);
    }

    @Transactional
    public SessionDto end(UUID id) {
        GamingSession session = requireSessionForUpdate(id);
        if (session.getStatus() == SessionStatus.COMPLETED) return toDto(session, Instant.now());
        requireStatus(session, SessionStatus.ACTIVE, SessionStatus.PAUSED);
        Instant now = Instant.now();
        long billableSeconds = SessionBillingCalculator.billableSeconds(session, now);
        session.complete(now, SessionBillingCalculator.amountFor(session.getHourlyRate(), billableSeconds));
        releasePc(session);
        return toDto(session, now);
    }

    @Transactional
    public void completeIfExpired(UUID id, Instant now) {
        GamingSession session = sessions.findByIdForUpdate(id).orElse(null);
        if (session == null || session.getStatus() != SessionStatus.ACTIVE || now.isBefore(session.expiryAt())) return;
        completeAtExpiry(session);
    }

    private void completeAtExpiry(GamingSession session) {
        Instant expiryAt = session.expiryAt();
        session.complete(expiryAt, SessionBillingCalculator.amountFor(session.getHourlyRate(), session.getAllocatedSeconds()));
        releasePc(session);
    }

    private void releasePc(GamingSession session) {
        pcs.findByIdForUpdate(session.getPc().getId()).ifPresent(pc -> pc.releaseFromSession(Instant.now()));
    }

    private SessionDto toDto(GamingSession session, Instant now) {
        long remaining = 0;
        if (session.getStatus() == SessionStatus.ACTIVE || session.getStatus() == SessionStatus.PAUSED) {
            remaining = Math.max(0, session.getAllocatedSeconds() - SessionBillingCalculator.billableSeconds(session, now));
        }
        Instant expiresAt = session.getStatus() == SessionStatus.PAUSED ? null : session.expiryAt();
        return new SessionDto(session.getId(), session.getBranch().getId(), session.getPc().getId(),
                session.getPc().getName(), session.getPricingPlan().getId(), session.getPricingPlan().getName(),
                session.getStatus().name(), session.getHourlyRate(), session.getPricingPlan().getCurrency(),
                session.getAllocatedSeconds(), remaining, session.getStartedAt(), session.getPausedAt(),
                session.getAccumulatedPausedSeconds(), expiresAt, session.getEndedAt(), session.getAmountDue());
    }

    private Branch requireBranch(UUID id) {
        return branches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
    }
    private GamingSession requireSessionForUpdate(UUID id) {
        return sessions.findByIdForUpdate(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }
    private static void requireStatus(GamingSession session, SessionStatus... accepted) {
        for (SessionStatus status : accepted) if (session.getStatus() == status) return;
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid transition from " + session.getStatus());
    }

    public record StartSessionRequest(UUID pcId, UUID pricingPlanId, long allocatedSeconds) {}
    public record SessionDto(UUID id, UUID branchId, UUID pcId, String pcName, UUID pricingPlanId, String pricingPlanName,
                             String status, BigDecimal hourlyRate, String currency, long allocatedSeconds,
                             long remainingSeconds, Instant startedAt, Instant pausedAt, long accumulatedPausedSeconds,
                             Instant expiresAt, Instant endedAt, BigDecimal amountDue) {}
}
