package com.cafeos.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

public final class SessionBillingCalculator {
    private SessionBillingCalculator() {}

    public static long billableSeconds(GamingSession session, Instant now) {
        long elapsed = Math.max(0, Duration.between(session.getStartedAt(), now).getSeconds()
                - session.getAccumulatedPausedSeconds());
        if (session.getStatus() == SessionStatus.PAUSED && session.getPausedAt() != null) {
            elapsed -= Math.max(0, Duration.between(session.getPausedAt(), now).getSeconds());
        }
        return Math.min(session.getAllocatedSeconds(), Math.max(0, elapsed));
    }

    public static BigDecimal amountFor(BigDecimal hourlyRate, long seconds) {
        return hourlyRate.multiply(BigDecimal.valueOf(seconds))
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }
}
