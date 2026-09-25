package com.dairy.demo.repository;

import com.dairy.demo.model.LagwadType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LagwadTypeRepository extends JpaRepository<LagwadType, Long> {

    List<LagwadType> findByBranchIdOrderByNameAsc(Long branchId);

    boolean existsByBranchIdAndNameIgnoreCase(Long branchId, String name);

    boolean existsByBranchIdAndNameIgnoreCaseAndIdNot(Long branchId, String name, Long id);
}