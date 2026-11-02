package com.cafeos.session;

import com.cafeos.branch.Branch;
import com.cafeos.pc.GamingPc;
import com.cafeos.pricing.PricingPlan;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "gaming_sessions")
public class GamingSession {
    @Id @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pc_id", nullable = false)
    private GamingPc pc;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pricing_plan_id", nullable = false)
    private PricingPlan pricingPlan;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SessionStatus status;
    @Column(name = "hourly_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;
    @Column(name = "allocated_seconds", nullable = false)
    private long allocatedSeconds;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "paused_at")
    private Instant pausedAt;
    @Column(name = "accumulated_paused_seconds", nullable = false)
    private long accumulatedPausedSeconds;
    @Column(name = "ended_at")
    private Instant endedAt;
    @Column(name = "amount_due", precision = 12, scale = 2)
    private BigDecimal amountDue;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected GamingSession() {}
    public GamingSession(Branch branch, GamingPc pc, PricingPlan pricingPlan, Instant startedAt, long allocatedSeconds) {
        this.branch = branch; this.pc = pc; this.pricingPlan = pricingPlan;
        this.status = SessionStatus.ACTIVE; this.hourlyRate = pricingPlan.getHourlyRate();
        this.startedAt = startedAt; this.allocatedSeconds = allocatedSeconds;
    }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public Branch getBranch() { return branch; }
    public GamingPc getPc() { return pc; }
    public PricingPlan getPricingPlan() { return pricingPlan; }
    public SessionStatus getStatus() { return status; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public long getAllocatedSeconds() { return allocatedSeconds; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getPausedAt() { return pausedAt; }
    public long getAccumulatedPausedSeconds() { return accumulatedPausedSeconds; }
    public Instant getEndedAt() { return endedAt; }
    public BigDecimal getAmountDue() { return amountDue; }
    public Instant expiryAt() { return startedAt.plusSeconds(allocatedSeconds + accumulatedPausedSeconds); }
    public void pause(Instant at) { status = SessionStatus.PAUSED; pausedAt = at; }
    public void resume(Instant at) {
        accumulatedPausedSeconds += Math.max(0, at.getEpochSecond() - pausedAt.getEpochSecond());
        pausedAt = null; status = SessionStatus.ACTIVE;
    }
    public void extend(long addedSeconds) { allocatedSeconds += addedSeconds; }
    public void complete(Instant at, BigDecimal amount) {
        if (pausedAt != null) accumulatedPausedSeconds += Math.max(0, at.getEpochSecond() - pausedAt.getEpochSecond());
        status = SessionStatus.COMPLETED; endedAt = at; amountDue = amount; pausedAt = null;
    }
}
