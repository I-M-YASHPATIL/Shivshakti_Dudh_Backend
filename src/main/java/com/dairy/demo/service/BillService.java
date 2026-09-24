package com.dairy.demo.service;

import com.dairy.demo.dto.DairyDTOs;
import com.dairy.demo.model.Bill;
import com.dairy.demo.model.Farmer;
import com.dairy.demo.model.MilkEntry;
import com.dairy.demo.repository.BillRepository;
import com.dairy.demo.repository.FarmerRepository;
import com.dairy.demo.repository.MilkEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BillService {

    private final BillRepository billRepository;
    private final FarmerRepository farmerRepository;
    private final MilkEntryRepository milkEntryRepository;
    private final MilkEntryService milkEntryService;
    private final FarmerService farmerService;

    private static final BigDecimal DEFAULT_SAVING_PERCENT = new BigDecimal("3.00");
    private static final BigDecimal SADILVAR_DEDUCTION = new BigDecimal("6.00");


    @Transactional
    public List<DairyDTOs.BillResponse> generateBillsForAllFarmers(
            String branchCode, DairyDTOs.BillGenerateRequest request) {

        Long branchId = farmerService.getBranchIdByCode(branchCode);
        List<Farmer> farmers = farmerRepository
                .findByBranchIdAndIsActiveTrueOrderByFarmerNumberAsc(branchId);

        log.info("Generating bills for branch={}, {} farmers: {} to {}",
                branchCode, farmers.size(), request.getFromDate(), request.getToDate());

        List<DairyDTOs.BillResponse> bills = new ArrayList<>();
        for (Farmer farmer : farmers) {
            DairyDTOs.BillResponse bill = generateBillForFarmer(farmer, request, true);
            if (bill.getTotalLiters().compareTo(BigDecimal.ZERO) > 0) {
                bills.add(bill);
            }
        }
        log.info("Generated {} bills with milk entries for branch={}", bills.size(), branchCode);
        return bills;
    }

    @Transactional
    public DairyDTOs.BillResponse generateBillForFarmerNumber(
            String branchCode, Integer farmerNumber, DairyDTOs.BillGenerateRequest request) {
        Farmer farmer = farmerService.findFarmerEntityByNumber(branchCode, farmerNumber);
        return generateBillForFarmer(farmer, request, true);
    }

    private DairyDTOs.BillResponse generateBillForFarmer(
            Farmer farmer, DairyDTOs.BillGenerateRequest request, boolean includeEntries) {

        List<MilkEntry> entries = milkEntryRepository
                .findByFarmerIdAndEntryDateBetweenOrderByEntryDateAscSessionAsc(
                        farmer.getId(), request.getFromDate(), request.getToDate());

        // ── Session totals ──────────────────────────────────────────────────────
        BigDecimal morningLiters = BigDecimal.ZERO, morningAmount = BigDecimal.ZERO;
        BigDecimal eveningLiters = BigDecimal.ZERO, eveningAmount = BigDecimal.ZERO;

        // ── Milk type totals ────────────────────────────────────────────────────
        BigDecimal cowLiters  = BigDecimal.ZERO, cowAmount  = BigDecimal.ZERO;
        BigDecimal buffLiters = BigDecimal.ZERO, buffAmount = BigDecimal.ZERO;

        for (MilkEntry e : entries) {
            if (e.getSession() == MilkEntry.Session.MORNING) {
                morningLiters = morningLiters.add(e.getLiters());
                morningAmount = morningAmount.add(e.getAmount());
            } else {
                eveningLiters = eveningLiters.add(e.getLiters());
                eveningAmount = eveningAmount.add(e.getAmount());
            }
            if (e.getMilkType() == MilkEntry.MilkType.COW) {
                cowLiters = cowLiters.add(e.getLiters());
                cowAmount = cowAmount.add(e.getAmount());
            } else {
                buffLiters = buffLiters.add(e.getLiters());
                buffAmount = buffAmount.add(e.getAmount());
            }
        }

        BigDecimal totalLiters = morningLiters.add(eveningLiters);
        BigDecimal totalAmount = morningAmount.add(eveningAmount);

        BigDecimal savingPct = nvl(request.getSavingPercent());
        if (savingPct.compareTo(BigDecimal.ZERO) == 0) savingPct = DEFAULT_SAVING_PERCENT;

        BigDecimal savingDeduction = totalAmount
                .multiply(savingPct)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal advance  = nvl(request.getAdvanceDeduction());
        BigDecimal other    = nvl(request.getOtherDeductions());

        // netAmount = totalAmount - savingDeduction - sadilvar(6) - advance - otherDeductions
        BigDecimal netAmount = totalAmount
                .subtract(savingDeduction)
                .subtract(SADILVAR_DEDUCTION)
                .subtract(advance)
                .subtract(other);

        // Upsert bill
        Bill bill = billRepository.findByFarmerIdAndFromDateAndToDate(
                        farmer.getId(), request.getFromDate(), request.getToDate())
                .orElse(new Bill());

        bill.setFarmer(farmer);
        bill.setFromDate(request.getFromDate());
        bill.setToDate(request.getToDate());

        bill.setMorningTotalLiters(morningLiters.setScale(2, RoundingMode.HALF_UP));
        bill.setMorningTotalAmount(morningAmount.setScale(2, RoundingMode.HALF_UP));
        bill.setEveningTotalLiters(eveningLiters.setScale(2, RoundingMode.HALF_UP));
        bill.setEveningTotalAmount(eveningAmount.setScale(2, RoundingMode.HALF_UP));

        bill.setCowTotalLiters(cowLiters.setScale(2, RoundingMode.HALF_UP));
        bill.setCowTotalAmount(cowAmount.setScale(2, RoundingMode.HALF_UP));
        bill.setBuffaloTotalLiters(buffLiters.setScale(2, RoundingMode.HALF_UP));
        bill.setBuffaloTotalAmount(buffAmount.setScale(2, RoundingMode.HALF_UP));

        bill.setTotalLiters(totalLiters.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        bill.setSavingPercent(savingPct.setScale(2, RoundingMode.HALF_UP));
        bill.setSavingDeduction(savingDeduction.setScale(2, RoundingMode.HALF_UP));
        bill.setSadilvar(SADILVAR_DEDUCTION);
        bill.setAdvanceDeduction(advance.setScale(2, RoundingMode.HALF_UP));
        bill.setOtherDeductions(other.setScale(2, RoundingMode.HALF_UP));
        bill.setNetAmount(netAmount.setScale(2, RoundingMode.HALF_UP));
        bill.setGeneratedDate(LocalDate.now());

        bill = billRepository.save(bill);

        log.info("Bill generated: branch={}, farmer={}, period={} to {}, totalAmount={}, savingDeduction={}, sadilvar={}, net={}",
                farmer.getBranch().getCode(), farmer.getFarmerNumber(),
                request.getFromDate(), request.getToDate(),
                totalAmount, savingDeduction, SADILVAR_DEDUCTION, netAmount);

        return toDTO(bill, includeEntries ? entries : null);
    }

    // ─── Queries ───────────────────────────────────────────────────────────────

    public List<DairyDTOs.BillResponse> getBillsForFarmer(
            String branchCode, Integer farmerNumber) {
        Farmer farmer = farmerService.findFarmerEntityByNumber(branchCode, farmerNumber);
        return billRepository.findByFarmerIdOrderByFromDateDesc(farmer.getId())
                .stream().map(b -> toDTO(b, null)).collect(Collectors.toList());
    }

    public List<DairyDTOs.BillResponse> getBillsByDateRange(
            String branchCode, LocalDate from, LocalDate to) {
        Long branchId = farmerService.getBranchIdByCode(branchCode);
        return billRepository
                .findByFarmerBranchIdAndFromDateAndToDateOrderByFarmerFarmerNumberAsc(
                        branchId, from, to)
                .stream().map(b -> toDTO(b, null)).collect(Collectors.toList());
    }

    public DairyDTOs.BillResponse getBillForFarmerRange(
            String branchCode, Integer farmerNumber, LocalDate from, LocalDate to) {
        Farmer farmer = farmerService.findFarmerEntityByNumber(branchCode, farmerNumber);
        Bill bill = billRepository.findByFarmerIdAndFromDateAndToDate(farmer.getId(), from, to)
                .orElseThrow(() -> new RuntimeException(
                        "या कालावधीचे (" + from + " ते " + to + ") बिल सापडले नाही. आधी बिल तयार करा."));
        return toDTO(bill, null);
    }


    public DairyDTOs.PeriodSummaryResponse getPeriodSummary(
            String branchCode, LocalDate from, LocalDate to) {

        Long branchId = farmerService.getBranchIdByCode(branchCode);

        Object[] row = billRepository.getPeriodSummaryFullByBranch(branchId, from, to);

        DairyDTOs.PeriodSummaryResponse summary = new DairyDTOs.PeriodSummaryResponse();
        summary.setFromDate(from);
        summary.setToDate(to);

        if (row != null && row[0] != null) {
            summary.setGrandTotalLiters((BigDecimal) row[0]);
            summary.setGrandTotalAmount((BigDecimal) row[1]);
            summary.setGrandSavingDeduction((BigDecimal) row[2]);
            summary.setGrandNetAmount((BigDecimal) row[3]);
            summary.setTotalFarmers(((Long) row[4]).intValue());
            summary.setCowTotalLiters((BigDecimal) row[5]);
            summary.setCowTotalAmount((BigDecimal) row[6]);
            summary.setBuffaloTotalLiters((BigDecimal) row[7]);
            summary.setBuffaloTotalAmount((BigDecimal) row[8]);
            summary.setMorningTotalLiters((BigDecimal) row[9]);
            summary.setEveningTotalLiters((BigDecimal) row[10]);
        }

        return summary;
    }

    // ─── Mark Paid ─────────────────────────────────────────────────────────────

    @Transactional
    public DairyDTOs.BillResponse markAsPaid(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + billId));
        bill.setIsPaid(true);
        return toDTO(billRepository.save(bill), null);
    }

    // ─── DTO Mapping ───────────────────────────────────────────────────────────

    private DairyDTOs.BillResponse toDTO(Bill bill, List<MilkEntry> entries) {
        DairyDTOs.BillResponse dto = new DairyDTOs.BillResponse();
        dto.setId(bill.getId());
        dto.setBranchCode(bill.getFarmer().getBranch().getCode());
        dto.setFarmerNumber(bill.getFarmer().getFarmerNumber());
        dto.setFarmerName(bill.getFarmer().getName());
        dto.setFromDate(bill.getFromDate());
        dto.setToDate(bill.getToDate());

        dto.setMorningTotalLiters(bill.getMorningTotalLiters());
        dto.setMorningTotalAmount(bill.getMorningTotalAmount());
        dto.setEveningTotalLiters(bill.getEveningTotalLiters());
        dto.setEveningTotalAmount(bill.getEveningTotalAmount());

        dto.setCowTotalLiters(bill.getCowTotalLiters());
        dto.setCowTotalAmount(bill.getCowTotalAmount());
        dto.setBuffaloTotalLiters(bill.getBuffaloTotalLiters());
        dto.setBuffaloTotalAmount(bill.getBuffaloTotalAmount());

        dto.setTotalLiters(bill.getTotalLiters());
        dto.setTotalAmount(bill.getTotalAmount());

        dto.setSavingPercent(bill.getSavingPercent());
        dto.setSavingDeduction(bill.getSavingDeduction());
        dto.setSadilvar(bill.getSadilvar());
        dto.setAdvanceDeduction(bill.getAdvanceDeduction());
        dto.setOtherDeductions(bill.getOtherDeductions());
        dto.setNetAmount(bill.getNetAmount());
        dto.setIsPaid(bill.getIsPaid());

        if (entries != null) {
            dto.setEntries(entries.stream()
                    .map(milkEntryService::toDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}