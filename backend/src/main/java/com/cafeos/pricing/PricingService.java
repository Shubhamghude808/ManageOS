package com.cafeos.pricing;

import com.cafeos.branch.Branch;
import com.cafeos.branch.BranchRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PricingService {
    private final PricingPlanRepository plans;
    private final BranchRepository branches;
    public PricingService(PricingPlanRepository plans, BranchRepository branches) {
        this.plans = plans; this.branches = branches;
    }

    @Transactional(readOnly = true)
    public List<PricingPlanDto> list(UUID branchId) {
        requireBranch(branchId);
        return plans.findAllByBranchIdOrderByNameAsc(branchId).stream().map(PricingPlanDto::from).toList();
    }

    @Transactional
    public PricingPlanDto create(CreatePricingPlanRequest request) {
        Branch branch = requireBranch(request.branchId());
        return PricingPlanDto.from(plans.save(new PricingPlan(branch, request.name().trim(), request.hourlyRate(),
                request.durationMinutes(), request.currency().toUpperCase(java.util.Locale.ROOT))));
    }

    private Branch requireBranch(UUID id) {
        return branches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
    }

    public record CreatePricingPlanRequest(UUID branchId, String name, BigDecimal hourlyRate, int durationMinutes, String currency) {}
    public record PricingPlanDto(UUID id, UUID branchId, String name, BigDecimal hourlyRate, int durationMinutes, String currency, boolean active) {
        static PricingPlanDto from(PricingPlan plan) {
            return new PricingPlanDto(plan.getId(), plan.getBranch().getId(), plan.getName(),
                    plan.getHourlyRate(), plan.getDurationMinutes(), plan.getCurrency(), plan.isActive());
        }
    }
}
