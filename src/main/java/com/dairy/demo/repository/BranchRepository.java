package com.dairy.demo.repository;

import com.dairy.demo.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}