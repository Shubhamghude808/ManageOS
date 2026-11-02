package com.cafeos.branch;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findAllByOrderByNameAsc();
    boolean existsByCafeIdAndNameIgnoreCase(UUID cafeId, String name);
}
