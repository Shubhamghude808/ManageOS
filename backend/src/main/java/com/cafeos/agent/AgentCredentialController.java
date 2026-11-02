package com.cafeos.agent;

import com.cafeos.common.ApiResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pcs")
public class AgentCredentialController {
    private final AgentTokenService tokens;
    public AgentCredentialController(AgentTokenService tokens) { this.tokens = tokens; }

    @PostMapping("/{pcId}/agent-token")
    public ResponseEntity<ApiResponse<AgentTokenService.IssuedAgentToken>> issue(@PathVariable UUID pcId) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(tokens.issue(pcId)));
    }
}
