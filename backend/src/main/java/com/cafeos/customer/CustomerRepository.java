package com.cafeos.customer;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByBranchIdAndName(UUID branchId, String name);
    Optional<Customer> findByBranchIdAndPhone(UUID branchId, String phone);
}
