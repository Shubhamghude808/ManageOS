package com.cafeos.branch;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "cafes")
public class Cafe {
    @Id @UuidGenerator
    private UUID id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Cafe() {}
    public Cafe(String name) { this.name = name; }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getName() { return name; }
}
