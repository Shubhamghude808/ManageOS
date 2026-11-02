package com.cafeos.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
        @NotBlank @Size(max = 160) String cafeName,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 64) String timezone) {}
