package com.cafeos.pc;

import com.cafeos.branch.Branch;
import com.cafeos.branch.BranchRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PcService {
    private final GamingPcRepository pcs;
    private final BranchRepository branches;

    public PcService(GamingPcRepository pcs, BranchRepository branches) {
        this.pcs = pcs; this.branches = branches;
    }

    @Transactional(readOnly = true)
    public List<PcDto> list(UUID branchId) {
        requireBranch(branchId);
        return pcs.findAllByBranchIdAndDeletedFalseOrderByNameAsc(branchId).stream().map(PcDto::from).toList();
    }

    @Transactional
    public PcDto create(CreatePcRequest request) {
        Branch branch = requireBranch(request.branchId());
        String name = request.name().trim();
        if (pcs.existsByBranchIdAndNameIgnoreCase(branch.getId(), name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A PC with this name already exists in the branch");
        }
        try {
            return PcDto.from(pcs.save(new GamingPc(branch, name, clean(request.hostname()))));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A PC with this name already exists in the branch", exception);
        }
    }

    @Transactional
    public PcDto update(UUID id, UpdatePcRequest request) {
        GamingPc pc = pcs.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PC not found in branch"));
        if (!pc.getBranch().getId().equals(request.branchId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PC not found in branch");
        }
        String name = request.name().trim();
        if (pcs.existsByBranchIdAndNameIgnoreCase(request.branchId(), name)
                && !pc.getName().equalsIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A PC with this name already exists in the branch");
        }
        if (pcs.hasOpenSession(id)) {
            pc.update(name, clean(request.hostname()), PcStatus.OCCUPIED);
        } else {
            if (request.status() == PcStatus.OCCUPIED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OCCUPIED is controlled by the session engine");
            }
            pc.update(name, clean(request.hostname()), request.status());
        }
        return PcDto.from(pc);
    }

    @Transactional
    public void delete(UUID id) {
        GamingPc pc = pcs.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PC not found"));
        // Keep the PC row because heartbeats and sessions reference it. Deletion is an archive.
        pc.markDeleted();
    }

    private Branch requireBranch(UUID id) {
        return branches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
    }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record CreatePcRequest(UUID branchId, String name, String hostname) {}
    public record UpdatePcRequest(UUID branchId, String name, String hostname, PcStatus status) {}
    public record PcDto(UUID id, UUID branchId, String branchName, String name, String status, String hostname,
                        java.time.Instant createdAt, java.time.Instant lastHeartbeatAt, boolean agentTokenIssued) {
        static PcDto from(GamingPc pc) {
            return new PcDto(pc.getId(), pc.getBranch().getId(), pc.getBranch().getName(), pc.getName(),
                    pc.getStatus().name(), pc.getHostname(), pc.getCreatedAt(), pc.getLastHeartbeatAt(), pc.hasAgentCredential());
        }
    }
}
