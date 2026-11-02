package com.cafeos.branch;

import com.cafeos.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService service;
    public BranchController(BranchService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<BranchDto>> list() { return ApiResponse.ok(service.list()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BranchDto> create(@Valid @RequestBody CreateBranchRequest request) {
        return ApiResponse.ok(service.create(request));
    }

}
