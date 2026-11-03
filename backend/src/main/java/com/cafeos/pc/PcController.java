package com.cafeos.pc;

import com.cafeos.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pcs")
public class PcController {
    private final PcService service;
    public PcController(PcService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<PcService.PcDto>> list(@RequestParam UUID branchId) {
        return ApiResponse.ok(service.list(branchId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PcService.PcDto> create(@Valid @RequestBody CreatePcBody body) {
        return ApiResponse.ok(service.create(new PcService.CreatePcRequest(body.branchId(), body.name(), body.hostname())));
    }

    @PutMapping("/{id}")
    public ApiResponse<PcService.PcDto> update(@PathVariable UUID id, @Valid @RequestBody UpdatePcBody body) {
        return ApiResponse.ok(service.update(id, new PcService.UpdatePcRequest(body.branchId(), body.name(), body.hostname(), body.status())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    public record CreatePcBody(@NotNull UUID branchId, @NotBlank @Size(max = 100) String name, @Size(max = 255) String hostname) {}
    public record UpdatePcBody(@NotNull UUID branchId, @NotBlank @Size(max = 100) String name, @Size(max = 255) String hostname, @NotNull PcStatus status) {}
}
