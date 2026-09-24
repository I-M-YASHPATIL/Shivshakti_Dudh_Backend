package com.dairy.demo.repository;

import com.dairy.demo.model.MilkEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface MilkEntryRepository extends JpaRepository<MilkEntry, Long> {


    boolean existsByFarmerIdAndEntryDateAndSessionAndMilkType(
            Long farmerId, LocalDate date,
            MilkEntry.Session session, MilkEntry.MilkType milkType);

    boolean existsByFarmerIdAndEntryDateAndSessionAndMilkTypeAndIdNot(
            Long farmerId, LocalDate date,
            MilkEntry.Session session, MilkEntry.MilkType milkType, Long id);


    List<MilkEntry> findByFarmerIdAndEntryDateBetweenOrderByEntryDateAscSessionAsc(
            Long farmerId, LocalDate from, LocalDate to);


    List<MilkEntry> findByFarmerBranchIdAndEntryDateOrderByFarmerFarmerNumberAscSessionAsc(
            Long branchId, LocalDate date);

    List<MilkEntry> findByFarmerBranchIdAndEntryDateAndSessionOrderByFarmerFarmerNumberAsc(
            Long branchId, LocalDate date, MilkEntry.Session session);

    List<MilkEntry> findByFarmerBranchIdAndEntryDateBetweenOrderByFarmerFarmerNumberAscEntryDateAscSessionAsc(
            Long branchId, LocalDate from, LocalDate to);


    @Query("""
        SELECT e FROM MilkEntry e
        WHERE e.farmer.branch.id = :branchId
          AND e.entryDate = :date
          AND (:session  IS NULL OR e.session  = :session)
          AND (:milkType IS NULL OR e.milkType = :milkType)
        ORDER BY e.farmer.farmerNumber ASC, e.session ASC
        """)
    List<MilkEntry> findByBranchDateSessionMilkType(
            @Param("branchId") Long branchId,
            @Param("date")     LocalDate date,
            @Param("session")  MilkEntry.Session session,
            @Param("milkType") MilkEntry.MilkType milkType);


    @Query("""
        SELECT m.farmer.id, m.session,
               SUM(m.liters), SUM(m.amount), COUNT(m)
        FROM MilkEntry m
        WHERE m.farmer.branch.id = :branchId
          AND m.entryDate BETWEEN :from AND :to
        GROUP BY m.farmer.id, m.session
        ORDER BY m.farmer.id, m.session
        """)
    List<Object[]> getSummaryByFarmerAndSession(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);

    @Query("""
        SELECT m.milkType,
               SUM(m.liters), SUM(m.amount),
               COUNT(DISTINCT m.farmer.id)
        FROM MilkEntry m
        WHERE m.farmer.branch.id = :branchId
          AND m.entryDate BETWEEN :from AND :to
        GROUP BY m.milkType
        ORDER BY m.milkType
        """)
    List<Object[]> getPeriodTotalsByMilkTypeAndBranch(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);

    @Query("""
        SELECT e FROM MilkEntry e
        WHERE e.farmer.branch.id    = :branchId
          AND e.farmer.farmerNumber = :farmerNumber
          AND e.entryDate BETWEEN :from AND :to
        ORDER BY e.entryDate ASC, e.session ASC
        """)
    List<MilkEntry> findByBranchAndFarmerNumberAndDateRange(
            @Param("branchId")     Long      branchId,
            @Param("farmerNumber") Integer   farmerNumber,
            @Param("from")         LocalDate from,
            @Param("to")           LocalDate to);


    @Query("""
        SELECT e FROM MilkEntry e
        WHERE e.farmer.branch.id = :branchId
          AND e.farmer.id        = :farmerId
          AND e.entryDate BETWEEN :from AND :to
        ORDER BY e.entryDate ASC, e.session ASC
        """)
    List<MilkEntry> findByBranchIdAndFarmerIdAndDateRange(
            @Param("branchId")  Long      branchId,
            @Param("farmerId")  Long      farmerId,
            @Param("from")      LocalDate from,
            @Param("to")        LocalDate to);

    @Query("""
        SELECT e.entryDate, e.session, e.milkType,
               SUM(e.liters), SUM(e.amount), COUNT(e)
        FROM MilkEntry e
        WHERE e.farmer.branch.id    = :branchId
          AND e.farmer.farmerNumber = :farmerNumber
          AND e.entryDate BETWEEN :from AND :to
        GROUP BY e.entryDate, e.session, e.milkType
        ORDER BY e.entryDate ASC, e.session ASC
        """)
    List<Object[]> getYearlySummaryByFarmerNumber(
            @Param("branchId")     Long      branchId,
            @Param("farmerNumber") Integer   farmerNumber,
            @Param("from")         LocalDate from,
            @Param("to")           LocalDate to);
}