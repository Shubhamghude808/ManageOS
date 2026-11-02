package com.cafeos.pricing;

import com.cafeos.branch.Branch;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pricing_plans")
public class PricingPlan {
    @Id @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "hourly_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currency;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PricingPlan() {}
    public PricingPlan(Branch branch, String name, BigDecimal hourlyRate, String currency) {
        this.branch = branch; this.name = name; this.hourlyRate = hourlyRate; this.currency = currency;
    }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public Branch getBranch() { return branch; }
    public String getName() { return name; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public String getCurrency() { return currency; }
    public boolean isActive() { return active; }
}
