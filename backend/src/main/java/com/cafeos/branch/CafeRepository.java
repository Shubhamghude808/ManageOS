package com.cafeos.branch;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeRepository extends JpaRepository<Cafe, UUID> {
    Optional<Cafe> findFirstByNameIgnoreCase(String name);
}
