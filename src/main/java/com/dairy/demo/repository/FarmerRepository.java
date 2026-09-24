package com.dairy.demo.repository;

import com.dairy.demo.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    List<Farmer> findByBranchIdAndIsActiveTrueOrderByFarmerNumberAsc(Long branchId);

    List<Farmer> findByBranchIdOrderByFarmerNumberAsc(Long branchId);

    Optional<Farmer> findByBranchIdAndFarmerNumber(Long branchId, Integer farmerNumber);

    boolean existsByBranchIdAndFarmerNumber(Long branchId, Integer farmerNumber);

    @Query("""
        SELECT f FROM Farmer f
        WHERE f.branch.id = :branchId
          AND (CAST(f.farmerNumber AS string) LIKE %:q%
               OR LOWER(f.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR f.phone LIKE %:q%)
        ORDER BY f.farmerNumber ASC
        """)
    List<Farmer> searchFarmers(@Param("branchId") Long branchId, @Param("q") String q);
}