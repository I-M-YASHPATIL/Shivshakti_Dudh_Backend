package com.dairy.demo.controller;

import com.dairy.demo.dto.DairyDTOs;
import com.dairy.demo.model.MilkEntry;
import com.dairy.demo.service.BillService;
import com.dairy.demo.service.BranchService;
import com.dairy.demo.service.FarmerService;
import com.dairy.demo.service.FatRateService;
import com.dairy.demo.service.LedgerService;
import com.dairy.demo.service.MilkEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class DairyController {

    private final FarmerService     farmerService;
    private final MilkEntryService  milkEntryService;
    private final BillService       billService;
    private final FatRateService    fatRateService;
    private final BranchService     branchService;
    private final LedgerService     ledgerService;

    // ── BRANCHES ──────────────────────────────────────────────────────────────

    @GetMapping("/branches")
    public ResponseEntity<List<DairyDTOs.BranchDTO>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    @PostMapping("/branches")
    public ResponseEntity<DairyDTOs.BranchDTO> createBranch(
            @Valid @RequestBody DairyDTOs.BranchDTO dto) {
        return ResponseEntity.ok(branchService.createBranch(dto));
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<DairyDTOs.BranchDTO> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody DairyDTOs.BranchDTO dto) {
        return ResponseEntity.ok(branchService.updateBranch(id, dto));
    }

    // ── FARMERS ───────────────────────────────────────────────────────────────

    @GetMapping("/{branchCode}/farmers")
    public ResponseEntity<List<DairyDTOs.FarmerDTO>> getAllFarmers(
            @PathVariable String branchCode,
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        if (all) return ResponseEntity.ok(farmerService.getAllFarmersIncludingInactive(branchCode));
        return ResponseEntity.ok(farmerService.getAllFarmers(branchCode));
    }

    @GetMapping("/{branchCode}/farmers/{number}")
    public ResponseEntity<DairyDTOs.FarmerDTO> getFarmerByNumber(
            @PathVariable String branchCode,
            @PathVariable Integer number) {
        return ResponseEntity.ok(farmerService.getFarmerByNumber(branchCode, number));
    }

    @GetMapping("/{branchCode}/farmers/search")
    public ResponseEntity<List<DairyDTOs.FarmerDTO>> searchFarmers(
            @PathVariable String branchCode,
            @RequestParam String q) {
        return ResponseEntity.ok(farmerService.searchFarmers(branchCode, q));
    }

    @PostMapping("/{branchCode}/farmers")
    public ResponseEntity<DairyDTOs.FarmerDTO> createFarmer(
            @PathVariable String branchCode,
            @Valid @RequestBody DairyDTOs.FarmerDTO dto) {
        return ResponseEntity.ok(farmerService.createFarmer(branchCode, dto));
    }

    @PutMapping("/{branchCode}/farmers/{id}")
    public ResponseEntity<DairyDTOs.FarmerDTO> updateFarmer(
            @PathVariable String branchCode,
            @PathVariable Long id,
            @Valid @RequestBody DairyDTOs.FarmerDTO dto) {
        return ResponseEntity.ok(farmerService.updateFarmer(id, dto));
    }

    // Soft delete — marks farmer as inactive
    @DeleteMapping("/{branchCode}/farmers/{number}")
    public ResponseEntity<DairyDTOs.FarmerDTO> deleteFarmer(
            @PathVariable String branchCode,
            @PathVariable Integer number) {
        return ResponseEntity.ok(farmerService.deleteFarmer(branchCode, number));
    }

    // Hard delete — permanently removes farmer record
    @DeleteMapping("/{branchCode}/farmers/{id}/permanent")
    public ResponseEntity<Void> hardDeleteFarmer(
            @PathVariable String branchCode,
            @PathVariable Long id) {
        farmerService.hardDeleteFarmer(id);
        return ResponseEntity.ok().build();
    }

    // ── FAT RATES ─────────────────────────────────────────────────────────────

    @GetMapping("/{branchCode}/fat-rates")
    public ResponseEntity<List<DairyDTOs.FatRateDTO>> getAllFatRates(
            @PathVariable String branchCode) {
        return ResponseEntity.ok(fatRateService.getAllRates(branchCode));
    }

    @GetMapping("/{branchCode}/fat-rates/by-type")
    public ResponseEntity<List<DairyDTOs.FatRateDTO>> getFatRatesByMilkType(
            @PathVariable String branchCode,
            @RequestParam String milkType) {
        return ResponseEntity.ok(fatRateService.getRatesByMilkType(branchCode, milkType));
    }

    @PostMapping("/{branchCode}/fat-rates")
    public ResponseEntity<DairyDTOs.FatRateDTO> saveFatRate(
            @PathVariable String branchCode,
            @Valid @RequestBody DairyDTOs.FatRateDTO dto) {
        return ResponseEntity.ok(fatRateService.saveRate(branchCode, dto));
    }

    @DeleteMapping("/{branchCode}/fat-rates/{id}")
    public ResponseEntity<Void> deleteFatRate(
            @PathVariable String branchCode,
            @PathVariable Long id) {
        fatRateService.deleteRate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{branchCode}/fat-rates/lookup")
    public ResponseEntity<DairyDTOs.FatLookupResponse> lookupRate(
            @PathVariable String branchCode,
            @Valid @RequestBody DairyDTOs.FatLookupRequest request) {
        return ResponseEntity.ok(milkEntryService.lookupRate(branchCode, request));
    }

    @PutMapping("/{branchCode}/fat-rates/{id}")
    public ResponseEntity<DairyDTOs.FatRateDTO> updateFatRate(
            @PathVariable String branchCode,
            @PathVariable Long id,
            @Valid @RequestBody DairyDTOs.FatRateDTO dto) {
        return ResponseEntity.ok(fatRateService.updateRate(branchCode, id, dto));
    }

    // ── MILK ENTRIES ──────────────────────────────────────────────────────────

    @PostMapping("/{branchCode}/entries")
    public ResponseEntity<DairyDTOs.MilkEntryResponse> createEntry(
            @PathVariable String branchCode,
            @Valid @RequestBody DairyDTOs.MilkEntryRequest request) {
        return ResponseEntity.ok(milkEntryService.createEntry(branchCode, request));
    }

    @GetMapping("/{branchCode}/entries/date/{date}")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getEntriesByDate(
            @PathVariable String branchCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(milkEntryService.getEntriesByDate(branchCode, date));
    }

    @GetMapping("/{branchCode}/entries/date/{date}/session/{session}")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getEntriesByDateAndSession(
            @PathVariable String branchCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String session) {
        MilkEntry.Session s = MilkEntry.Session.valueOf(session.toUpperCase());
        return ResponseEntity.ok(milkEntryService.getEntriesByDateAndSession(branchCode, date, s));
    }

    @GetMapping("/{branchCode}/entries/date/{date}/type/{milkType}")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getEntriesByDateAndMilkType(
            @PathVariable String branchCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String milkType) {
        MilkEntry.MilkType type = MilkEntry.MilkType.valueOf(milkType.toUpperCase());
        return ResponseEntity.ok(
                milkEntryService.getEntriesByDateSessionAndMilkType(branchCode, date, null, type));
    }

    @GetMapping("/{branchCode}/entries/date/{date}/session/{session}/type/{milkType}")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getEntriesByDateSessionAndMilkType(
            @PathVariable String branchCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String session,
            @PathVariable String milkType) {
        MilkEntry.Session  s    = MilkEntry.Session.valueOf(session.toUpperCase());
        MilkEntry.MilkType type = MilkEntry.MilkType.valueOf(milkType.toUpperCase());
        return ResponseEntity.ok(
                milkEntryService.getEntriesByDateSessionAndMilkType(branchCode, date, s, type));
    }

    @GetMapping("/{branchCode}/entries/farmer/{number}")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getEntriesByFarmer(
            @PathVariable String branchCode,
            @PathVariable Integer number,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(
                milkEntryService.getEntriesByFarmerAndDateRange(branchCode, number, from, to));
    }

    @GetMapping("/{branchCode}/entries/range")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getEntriesByRange(
            @PathVariable String branchCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(milkEntryService.getEntriesByDateRange(branchCode, from, to));
    }

    @DeleteMapping("/{branchCode}/entries/{id}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable String branchCode,
            @PathVariable Long id) {
        milkEntryService.deleteEntry(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{branchCode}/entries/yearly")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getYearlyEntriesForFarmer(
            @PathVariable  String    branchCode,
            @RequestParam  Integer   farmerNumber,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                milkEntryService.getYearlyEntriesForFarmer(branchCode, farmerNumber, from, to));
    }
    @GetMapping("/{branchCode}/entries/yearly/by-id")
    public ResponseEntity<List<DairyDTOs.MilkEntryResponse>> getYearlyEntriesForFarmerById(
            @PathVariable  String    branchCode,
            @RequestParam  Long      farmerId,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                milkEntryService.getYearlyEntriesForFarmerById(branchCode, farmerId, from, to));
    }

    // ── BILLS ─────────────────────────────────────────────────────────────────

    @PostMapping("/{branchCode}/bills/generate")
    public ResponseEntity<List<DairyDTOs.BillResponse>> generateBills(
            @PathVariable String branchCode,
            @Valid @RequestBody DairyDTOs.BillGenerateRequest request) {
        return ResponseEntity.ok(billService.generateBillsForAllFarmers(branchCode, request));
    }

    @PostMapping("/{branchCode}/bills/generate/{number}")
    public ResponseEntity<DairyDTOs.BillResponse> generateBillForFarmer(
            @PathVariable String branchCode,
            @PathVariable Integer number,
            @Valid @RequestBody DairyDTOs.BillGenerateRequest request) {
        return ResponseEntity.ok(
                billService.generateBillForFarmerNumber(branchCode, number, request));
    }

    @GetMapping("/{branchCode}/bills/farmer/{number}")
    public ResponseEntity<List<DairyDTOs.BillResponse>> getBillsForFarmer(
            @PathVariable String branchCode,
            @PathVariable Integer number) {
        return ResponseEntity.ok(billService.getBillsForFarmer(branchCode, number));
    }

    @GetMapping("/{branchCode}/bills/range")
    public ResponseEntity<List<DairyDTOs.BillResponse>> getBillsByRange(
            @PathVariable String branchCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(billService.getBillsByDateRange(branchCode, from, to));
    }

    @GetMapping("/{branchCode}/bills/summary")
    public ResponseEntity<DairyDTOs.PeriodSummaryResponse> getPeriodSummary(
            @PathVariable String branchCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(billService.getPeriodSummary(branchCode, from, to));
    }

    @PatchMapping("/bills/{id}/paid")
    public ResponseEntity<DairyDTOs.BillResponse> markBillAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(billService.markAsPaid(id));
    }

    // ── LEDGER (उचल / लागवड / जमा / बाकी) ────────────────────────────────────

    @GetMapping("/{branchCode}/ledger/{number}")
    public ResponseEntity<DairyDTOs.LedgerResponse> getLedger(
            @PathVariable String branchCode,
            @PathVariable Integer number) {
        return ResponseEntity.ok(ledgerService.getLedger(branchCode, number));
    }

    // Looks up the milk bill for a farmer for a given period (e.g. 01/09/2026 -
    // 10/09/2026) so its net amount can be shown before recording it as जमा.
    @GetMapping("/{branchCode}/ledger/{number}/bill-lookup")
    public ResponseEntity<DairyDTOs.BillResponse> lookupBillForLedger(
            @PathVariable String branchCode,
            @PathVariable Integer number,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(billService.getBillForFarmerRange(branchCode, number, from, to));
    }

    @PostMapping("/{branchCode}/ledger/{number}")
    public ResponseEntity<DairyDTOs.LedgerResponse> addLedgerEntry(
            @PathVariable String branchCode,
            @PathVariable Integer number,
            @Valid @RequestBody DairyDTOs.LedgerEntryRequest request) {
        return ResponseEntity.ok(ledgerService.addEntry(branchCode, number, request));
    }

    @DeleteMapping("/{branchCode}/ledger/{number}/{entryId}")
    public ResponseEntity<DairyDTOs.LedgerResponse> deleteLedgerEntry(
            @PathVariable String branchCode,
            @PathVariable Integer number,
            @PathVariable Long entryId) {
        return ResponseEntity.ok(ledgerService.deleteEntry(branchCode, number, entryId));
    }
    @PutMapping("/{branchCode}/entries/{id}")
public ResponseEntity<DairyDTOs.MilkEntryResponse> updateEntry(
        @PathVariable String branchCode,
        @PathVariable Long id,
        @Valid @RequestBody DairyDTOs.MilkEntryRequest request) {
    return ResponseEntity.ok(milkEntryService.updateEntry(branchCode, id, request));
}
}