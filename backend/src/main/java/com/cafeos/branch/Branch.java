package com.cafeos.branch;

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

@Entity
@Table(name = "branches")
public class Branch {
    @Id @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, length = 64)
    private String timezone;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Branch() {}
    public Branch(Cafe cafe, String name, String timezone) {
        this.cafe = cafe; this.name = name; this.timezone = timezone;
    }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public Cafe getCafe() { return cafe; }
    public String getTimezone() { return timezone; }
}
