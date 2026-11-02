package com.cafeos.agent;

import com.cafeos.pc.GamingPc;
import com.cafeos.pc.GamingPcRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AgentTokenService {
    private final GamingPcRepository pcs;
    private final SecureRandom secureRandom = new SecureRandom();

    public AgentTokenService(GamingPcRepository pcs) { this.pcs = pcs; }

    @Transactional
    public IssuedAgentToken issue(UUID pcId) {
        GamingPc pc = pcs.findById(pcId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PC not found"));
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        pc.setAgentCredentialHash(hash(token));
        return new IssuedAgentToken(pc.getId(), token);
    }

    @Transactional(readOnly = true)
    public Optional<UUID> authenticate(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return pcs.findByAgentCredentialHash(hash(token)).map(GamingPc::getId);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record IssuedAgentToken(UUID pcId, String token) {}
}
