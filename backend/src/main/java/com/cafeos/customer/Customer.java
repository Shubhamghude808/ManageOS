package com.cafeos.customer;

import com.cafeos.branch.Branch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 32)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Customer() {}

    public Customer(Branch branch, String name, String phone) {
        this.branch = branch;
        this.name = name;
        this.phone = phone;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Branch getBranch() { return branch; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
}
