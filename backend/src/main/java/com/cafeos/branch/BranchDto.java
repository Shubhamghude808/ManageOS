package com.cafeos.branch;

import java.util.UUID;

public record BranchDto(UUID id, String cafeName, String name, String timezone) {
    public static BranchDto from(Branch branch) {
        return new BranchDto(branch.getId(), branch.getCafe().getName(), branch.getName(), branch.getTimezone());
    }
}
