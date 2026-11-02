package com.cafeos.agent;

import com.cafeos.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final AgentService service;
    public AgentController(AgentService service) { this.service = service; }

    @PostMapping("/register")
    public ApiResponse<AgentService.AgentAcknowledgement> register(
            @AuthenticationPrincipal UUID pcId, @Valid @RequestBody RegistrationBody body) {
        return ApiResponse.ok(service.register(pcId, new AgentService.AgentRegistrationRequest(
                body.hostname(), body.os(), body.ipAddress(), body.agentVersion())));
    }

    @PostMapping("/heartbeat")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<AgentService.AgentAcknowledgement> heartbeat(
            @AuthenticationPrincipal UUID pcId, @Valid @RequestBody HeartbeatBody body) {
        return ApiResponse.ok(service.heartbeat(pcId, new AgentService.AgentHeartbeatRequest(
                body.agentStatus(), body.agentVersion(), body.cpuPercent(), body.memoryPercent())));
    }

    public record RegistrationBody(@NotBlank @Size(max = 255) String hostname,
                                   @NotBlank @Size(max = 100) String os,
                                   @NotBlank @Size(max = 64) String ipAddress,
                                   @NotBlank @Size(max = 40) String agentVersion) {}
    public record HeartbeatBody(@NotBlank @Size(max = 30) String agentStatus,
                                @NotBlank @Size(max = 40) String agentVersion,
                                @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal cpuPercent,
                                @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal memoryPercent) {}
}
