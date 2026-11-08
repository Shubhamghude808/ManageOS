package com.cafeos.pc;

import com.cafeos.branch.Branch;
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
@Table(name = "gaming_pcs")
public class GamingPc {
    @Id @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @Column(nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PcStatus status;
    @Column(length = 255)
    private String hostname;
    @Column(name = "agent_credential_hash", length = 255)
    private String agentCredentialHash;
    @Column(name = "agent_machine_id", length = 128)
    private String agentMachineId;
    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private boolean deleted;

    protected GamingPc() {}
    public GamingPc(Branch branch, String name, String hostname) {
        this.branch = branch; this.name = name; this.hostname = hostname; this.status = PcStatus.AVAILABLE;
    }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public Branch getBranch() { return branch; }
    public String getName() { return name; }
    public PcStatus getStatus() { return status; }
    public String getHostname() { return hostname; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastHeartbeatAt() { return lastHeartbeatAt; }
    public String getAgentCredentialHash() { return agentCredentialHash; }
    public String getAgentMachineId() { return agentMachineId; }
    public boolean hasAgentCredential() { return agentCredentialHash != null; }
    public boolean isDeleted() { return deleted; }
    public void setAgentCredentialHash(String hash) { this.agentCredentialHash = hash; }
    public void setAgentMachineId(String machineId) { this.agentMachineId = machineId; }
    public void markDeleted() {
        this.deleted = true;
        this.agentCredentialHash = null;
        this.agentMachineId = null;
    }
    public void registerAgent(String hostname) { this.hostname = hostname; }
    public void recordHeartbeat(Instant receivedAt) {
        recordHeartbeat(receivedAt, false);
    }
    public void recordHeartbeat(Instant receivedAt, boolean occupied) {
        this.lastHeartbeatAt = receivedAt;
        if (this.status == PcStatus.OFFLINE) this.status = occupied ? PcStatus.OCCUPIED : PcStatus.AVAILABLE;
    }
    public void markOccupied() { this.status = PcStatus.OCCUPIED; }
    public void releaseFromSession(Instant at) {
        if (this.status == PcStatus.OCCUPIED) {
            boolean heartbeatStale = lastHeartbeatAt != null && lastHeartbeatAt.isBefore(at.minusSeconds(45));
            this.status = heartbeatStale ? PcStatus.OFFLINE : PcStatus.AVAILABLE;
        }
    }
    public void update(String name, String hostname, PcStatus status) {
        this.name = name; this.hostname = hostname; this.status = status;
    }
}
