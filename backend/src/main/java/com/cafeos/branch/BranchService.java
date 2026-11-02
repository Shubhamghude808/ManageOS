package com.cafeos.branch;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BranchService {
    private final CafeRepository cafes;
    private final BranchRepository branches;

    public BranchService(CafeRepository cafes, BranchRepository branches) {
        this.cafes = cafes; this.branches = branches;
    }

    @Transactional(readOnly = true)
    public List<BranchDto> list() {
        return branches.findAllByOrderByNameAsc().stream().map(BranchDto::from).toList();
    }

    @Transactional
    public BranchDto create(CreateBranchRequest request) {
        Cafe cafe = cafes.findFirstByNameIgnoreCase(request.cafeName().trim())
                .orElseGet(() -> cafes.save(new Cafe(request.cafeName().trim())));
        if (branches.existsByCafeIdAndNameIgnoreCase(cafe.getId(), request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Branch already exists");
        }
        return BranchDto.from(branches.save(new Branch(cafe, request.name().trim(), request.timezone().trim())));
    }
}
