package com.cafeos.pricing;

import com.cafeos.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing-plans")
public class PricingController {
    private final PricingService service;
    public PricingController(PricingService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<PricingService.PricingPlanDto>> list(@RequestParam UUID branchId) {
        return ApiResponse.ok(service.list(branchId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PricingService.PricingPlanDto> create(@Valid @RequestBody CreatePricingPlanBody body) {
        return ApiResponse.ok(service.create(new PricingService.CreatePricingPlanRequest(
                body.branchId(), body.name(), body.hourlyRate(), body.currency())));
    }

    public record CreatePricingPlanBody(@NotNull UUID branchId, @NotBlank @Size(max = 120) String name,
                                        @NotNull @DecimalMin("0.01") BigDecimal hourlyRate,
                                        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency) {}
}
