package com.cafeos.agent;

import com.cafeos.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentEnrollmentController {
    private final AgentTokenService tokens;

    public AgentEnrollmentController(AgentTokenService tokens) { this.tokens = tokens; }

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(@Valid @RequestBody EnrollmentRequest request) {
        AgentTokenService.IssuedAgentToken issued = tokens.issueForEnrollment(
                request.machineId(), request.hostname(), request.branchId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new EnrollmentResponse(issued.pcId(), issued.token())));
    }

    public record EnrollmentRequest(@NotBlank @Size(max = 128) String machineId,
                                    @NotBlank @Size(max = 255) String hostname,
                                    @NotBlank @Size(max = 100) String os,
                                    @NotBlank @Size(max = 64) String ipAddress,
                                    @NotBlank @Size(max = 40) String agentVersion,
                                    UUID branchId) {}
    public record EnrollmentResponse(UUID pcId, String token) {}
}
