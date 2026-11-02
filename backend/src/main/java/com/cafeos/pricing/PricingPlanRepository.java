package com.cafeos.pricing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingPlanRepository extends JpaRepository<PricingPlan, UUID> {
    List<PricingPlan> findAllByBranchIdOrderByNameAsc(UUID branchId);
    Optional<PricingPlan> findByIdAndBranchIdAndActiveTrue(UUID id, UUID branchId);
}
