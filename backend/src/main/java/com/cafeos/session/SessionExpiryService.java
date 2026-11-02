package com.cafeos.session;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SessionExpiryService {
    private final GamingSessionRepository sessions;
    private final SessionService service;
    public SessionExpiryService(GamingSessionRepository sessions, SessionService service) {
        this.sessions = sessions; this.service = service;
    }

    @Scheduled(fixedDelay = 1_000)
    public void expireSessions() {
        Instant now = Instant.now();
        sessions.findAllByStatus(SessionStatus.ACTIVE).forEach(session -> service.completeIfExpired(session.getId(), now));
    }
}
