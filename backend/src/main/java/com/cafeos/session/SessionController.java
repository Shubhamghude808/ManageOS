package com.cafeos.session;

import com.cafeos.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final SessionService service;
    public SessionController(SessionService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<SessionService.SessionDto>> list(@RequestParam UUID branchId) {
        return ApiResponse.ok(service.list(branchId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SessionService.SessionDto> start(@Valid @RequestBody StartSessionBody body) {
        return ApiResponse.ok(service.start(new SessionService.StartSessionRequest(
                body.pcId(), body.pricingPlanId(), body.allocatedSeconds())));
    }

    @PostMapping("/{id}/pause")
    public ApiResponse<SessionService.SessionDto> pause(@PathVariable UUID id) { return ApiResponse.ok(service.pause(id)); }
    @PostMapping("/{id}/resume")
    public ApiResponse<SessionService.SessionDto> resume(@PathVariable UUID id) { return ApiResponse.ok(service.resume(id)); }
    @PostMapping("/{id}/extend")
    public ApiResponse<SessionService.SessionDto> extend(@PathVariable UUID id, @Valid @RequestBody ExtendSessionBody body) {
        return ApiResponse.ok(service.extend(id, body.addedSeconds()));
    }
    @PostMapping("/{id}/end")
    public ApiResponse<SessionService.SessionDto> end(@PathVariable UUID id) { return ApiResponse.ok(service.end(id)); }

    public record StartSessionBody(@NotNull UUID pcId, @NotNull UUID pricingPlanId,
                                   @Min(60) @Max(86_400) long allocatedSeconds) {}
    public record ExtendSessionBody(@Min(60) @Max(86_400) long addedSeconds) {}
}
