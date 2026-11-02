package com.cafeos.agent;

import com.cafeos.pc.GamingPc;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pc_heartbeats")
public class PcHeartbeat {
    @Id @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pc_id", nullable = false)
    private GamingPc pc;
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
    @Column(name = "agent_version", length = 40)
    private String agentVersion;
    @Column(name = "cpu_percent", precision = 5, scale = 2)
    private BigDecimal cpuPercent;
    @Column(name = "memory_percent", precision = 5, scale = 2)
    private BigDecimal memoryPercent;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> details;

    protected PcHeartbeat() {}
    public PcHeartbeat(GamingPc pc, Instant receivedAt, String agentVersion, BigDecimal cpuPercent,
                       BigDecimal memoryPercent, Map<String, Object> details) {
        this.pc = pc; this.receivedAt = receivedAt; this.agentVersion = agentVersion;
        this.cpuPercent = cpuPercent; this.memoryPercent = memoryPercent; this.details = details;
    }
}
