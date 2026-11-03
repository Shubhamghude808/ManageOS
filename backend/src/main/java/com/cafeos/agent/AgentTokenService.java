package com.cafeos.agent;

import com.cafeos.pc.GamingPc;
import com.cafeos.pc.GamingPcRepository;
import com.cafeos.branch.Branch;
import com.cafeos.branch.BranchRepository;
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
    private final BranchRepository branches;
    private final SecureRandom secureRandom = new SecureRandom();

    public AgentTokenService(GamingPcRepository pcs, BranchRepository branches) {
        this.pcs = pcs;
        this.branches = branches;
    }

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

    @Transactional
    public IssuedAgentToken issueForEnrollment(String machineId, String hostname, UUID branchId) {
        GamingPc pc = pcs.findByAgentMachineIdAndDeletedFalse(machineId)
                .or(() -> pcs.findByHostnameIgnoreCaseAndDeletedFalse(hostname))
                .orElseGet(() -> createPcForEnrollment(machineId, hostname, branchId));
        if (pc.getAgentMachineId() != null && !pc.getAgentMachineId().equals(machineId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gaming PC is already enrolled to another computer");
        }
        pc.setAgentMachineId(machineId);
        return issue(pc.getId());
    }

    private GamingPc createPcForEnrollment(String machineId, String hostname, UUID branchId) {
        Branch branch;
        if (branchId != null) {
            branch = branches.findById(branchId).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Branch not found"));
        } else {
            var availableBranches = branches.findAllByOrderByNameAsc();
            if (availableBranches.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Create a cafe branch before starting an agent");
            }
            if (availableBranches.size() > 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Set CAFEOS_BRANCH_ID when the cafe has multiple branches");
            }
            branch = availableBranches.getFirst();
        }

        String baseName = hostname.trim().isEmpty() ? "Gaming PC" : hostname.trim();
        String name = baseName.length() > 100 ? baseName.substring(0, 100) : baseName;
        if (pcs.existsByBranchIdAndNameIgnoreCaseAndDeletedFalse(branch.getId(), name)) {
            name = "Gaming PC " + machineId.substring(0, Math.min(8, machineId.length()));
        }
        return pcs.save(new GamingPc(branch, name, hostname.trim()));
    }

    @Transactional(readOnly = true)
    public Optional<UUID> authenticate(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return pcs.findByAgentCredentialHashAndDeletedFalse(hash(token)).map(GamingPc::getId);
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
