package com.dairy.demo.repository;

import com.dairy.demo.model.FatRate;
import com.dairy.demo.model.MilkEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FatRateRepository extends JpaRepository<FatRate, Long> {

    // All rates for a branch ordered by fat %
    List<FatRate> findByBranchIdOrderByFatPercentageAsc(Long branchId);

    // Rates for a branch filtered by milk type
    List<FatRate> findByBranchIdAndMilkTypeOrderByFatPercentageAsc(
            Long branchId, MilkEntry.MilkType milkType);

    // Rate lookup for a specific branch + fat% + milk type
    Optional<FatRate> findByBranchIdAndFatPercentageAndMilkType(
            Long branchId, BigDecimal fatPercentage, MilkEntry.MilkType milkType);

    Optional<FatRate> findByBranchIdAndFatPercentageAndMilkTypeAndSnf(
            Long branchId, BigDecimal fatPercentage, MilkEntry.MilkType milkType, BigDecimal snf);
}