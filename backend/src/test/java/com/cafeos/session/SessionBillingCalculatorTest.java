package com.cafeos.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cafeos.branch.Branch;
import com.cafeos.branch.Cafe;
import com.cafeos.pc.GamingPc;
import com.cafeos.pricing.PricingPlan;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SessionBillingCalculatorTest {
    private final Cafe cafe = new Cafe("Test cafe");
    private final Branch branch = new Branch(cafe, "Test branch", "Asia/Kolkata");
    private final GamingPc pc = new GamingPc(branch, "PC-01", null);
    private final PricingPlan plan = new PricingPlan(branch, "Standard", new BigDecimal("100.00"), "INR");

    @Test
    void pausedTimeDoesNotCountTowardElapsedBilling() {
        Instant started = Instant.parse("2026-01-01T10:00:00Z");
        GamingSession session = new GamingSession(branch, pc, plan, started, 3600);
        session.pause(Instant.parse("2026-01-01T10:30:00Z"));

        long billable = SessionBillingCalculator.billableSeconds(session, Instant.parse("2026-01-01T11:30:00Z"));
        assertEquals(1800, billable);
        assertEquals(new BigDecimal("50.00"), SessionBillingCalculator.amountFor(plan.getHourlyRate(), billable));
    }

    @Test
    void billableTimeIsCappedAtAllocationAndRoundedToCurrencyPrecision() {
        GamingSession session = new GamingSession(branch, pc, plan, Instant.parse("2026-01-01T10:00:00Z"), 3600);
        long billable = SessionBillingCalculator.billableSeconds(session, Instant.parse("2026-01-01T12:00:00Z"));
        assertEquals(3600, billable);
        assertEquals(new BigDecimal("100.00"), SessionBillingCalculator.amountFor(plan.getHourlyRate(), billable));
        assertEquals(new BigDecimal("0.03"), SessionBillingCalculator.amountFor(new BigDecimal("100"), 1));
    }

    @Test
    void resumingMovesExpiryByPausedDuration() {
        Instant started = Instant.parse("2026-01-01T10:00:00Z");
        GamingSession session = new GamingSession(branch, pc, plan, started, 3600);
        session.pause(Instant.parse("2026-01-01T10:30:00Z"));
        session.resume(Instant.parse("2026-01-01T11:00:00Z"));

        assertEquals(1800, session.getAccumulatedPausedSeconds());
        assertEquals(Instant.parse("2026-01-01T11:30:00Z"), session.expiryAt());
        assertEquals(3600, SessionBillingCalculator.billableSeconds(session, Instant.parse("2026-01-01T11:30:00Z")));
    }
}
