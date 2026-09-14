package com.dairy.demo.repository;

import com.dairy.demo.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // ─── Basic Finders ────────────────────────────────────────────────────────

    List<Bill> findByFarmerIdOrderByFromDateDesc(Long farmerId);

    Optional<Bill> findByFarmerIdAndFromDateAndToDate(
            Long farmerId, LocalDate from, LocalDate to);

    // ─── Branch-scoped Finders ─────────────────────────────────────────────────

    List<Bill> findByFarmerBranchIdAndFromDateAndToDateOrderByFarmerFarmerNumberAsc(
            Long branchId, LocalDate from, LocalDate to);

    List<Bill> findByFarmerBranchIdAndFromDateAndToDateAndIsPaidOrderByFarmerFarmerNumberAsc(
            Long branchId, LocalDate from, LocalDate to, Boolean isPaid);

    List<Bill> findByFarmerIdAndIsPaidOrderByFromDateDesc(
            Long farmerId, Boolean isPaid);

    // ─── Branch-scoped: Farmer date range ─────────────────────────────────────

    @Query("""
        SELECT b FROM Bill b
        WHERE b.farmer.id = :farmerId
          AND b.fromDate >= :from AND b.toDate <= :to
        ORDER BY b.fromDate DESC
        """)
    List<Bill> findByFarmerIdAndDateRangeWithin(
            @Param("farmerId") Long farmerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ─── Branch-scoped: Period Summary ─────────────────────────────────────────

    @Query("""
        SELECT SUM(b.totalLiters), SUM(b.totalAmount),
               SUM(b.savingDeduction), SUM(b.netAmount), COUNT(b)
        FROM Bill b
        WHERE b.farmer.branch.id = :branchId
          AND b.fromDate = :from AND b.toDate = :to
        """)
    Object[] getPeriodSummaryByBranch(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Full breakdown: cow/buffalo + session totals, scoped to branch
    @Query("""
        SELECT
            SUM(b.totalLiters),     SUM(b.totalAmount),
            SUM(b.savingDeduction), SUM(b.netAmount),    COUNT(b),
            SUM(b.cowTotalLiters),  SUM(b.cowTotalAmount),
            SUM(b.buffaloTotalLiters), SUM(b.buffaloTotalAmount),
            SUM(b.morningTotalLiters), SUM(b.eveningTotalLiters)
        FROM Bill b
        WHERE b.farmer.branch.id = :branchId
          AND b.fromDate = :from AND b.toDate = :to
        """)
    Object[] getPeriodSummaryFullByBranch(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ─── Branch-scoped: Unpaid summary per farmer ──────────────────────────────

    @Query("""
        SELECT b.farmer.farmerNumber, b.farmer.name, COUNT(b),
               SUM(b.savingDeduction), SUM(b.netAmount)
        FROM Bill b
        WHERE b.farmer.branch.id = :branchId
          AND b.isPaid = false
          AND b.fromDate >= :from AND b.toDate <= :to
        GROUP BY b.farmer.id, b.farmer.farmerNumber, b.farmer.name
        ORDER BY b.farmer.farmerNumber ASC
        """)
    List<Object[]> getUnpaidSummaryByFarmer(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}