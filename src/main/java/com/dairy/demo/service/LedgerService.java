package com.dairy.demo.service;

import com.dairy.demo.dto.DairyDTOs;
import com.dairy.demo.model.Farmer;
import com.dairy.demo.model.LedgerEntry;
import com.dairy.demo.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final FarmerService farmerService;

    public DairyDTOs.LedgerResponse getLedger(String branchCode, Integer farmerNumber) {
        Farmer farmer = farmerService.findFarmerEntityByNumber(branchCode, farmerNumber);
        List<LedgerEntry> entries =
                ledgerEntryRepository.findByFarmerIdOrderByEntryDateAscIdAsc(farmer.getId());
        return buildResponse(farmer, entries);
    }

    @Transactional
    public DairyDTOs.LedgerResponse addEntry(String branchCode, Integer farmerNumber,
                                              DairyDTOs.LedgerEntryRequest request) {
        Farmer farmer = farmerService.findFarmerEntityByNumber(branchCode, farmerNumber);
        LedgerEntry.Type type = parseType(request.getType());

        LedgerEntry entry = LedgerEntry.builder()
                .farmer(farmer)
                .type(type)
                .amount(request.getAmount())
                .entryDate(request.getEntryDate())
                .billFromDate(request.getBillFromDate())
                .billToDate(request.getBillToDate())
                .milkType(normalizeMilkType(request.getMilkType()))
                .note(request.getNote())
                .build();

        ledgerEntryRepository.save(entry);
        log.info("Ledger entry added: branch={}, farmer#{}, type={}, amount={}",
                branchCode, farmerNumber, type, request.getAmount());

        List<LedgerEntry> entries =
                ledgerEntryRepository.findByFarmerIdOrderByEntryDateAscIdAsc(farmer.getId());
        return buildResponse(farmer, entries);
    }

    @Transactional
    public DairyDTOs.LedgerResponse deleteEntry(String branchCode, Integer farmerNumber, Long entryId) {
        Farmer farmer = farmerService.findFarmerEntityByNumber(branchCode, farmerNumber);
        LedgerEntry entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Ledger entry not found: " + entryId));

        if (!entry.getFarmer().getId().equals(farmer.getId())) {
            throw new IllegalArgumentException(
                    "Entry #" + entryId + " does not belong to farmer #" + farmerNumber);
        }

        ledgerEntryRepository.delete(entry);
        log.info("Ledger entry deleted: branch={}, farmer#{}, entryId={}",
                branchCode, farmerNumber, entryId);

        List<LedgerEntry> entries =
                ledgerEntryRepository.findByFarmerIdOrderByEntryDateAscIdAsc(farmer.getId());
        return buildResponse(farmer, entries);
    }


    private DairyDTOs.LedgerResponse buildResponse(Farmer farmer, List<LedgerEntry> entries) {
        BigDecimal running = BigDecimal.ZERO;
        List<DairyDTOs.LedgerEntryResponse> entryDTOs = new ArrayList<>();

        for (LedgerEntry e : entries) {
            running = (e.getType() == LedgerEntry.Type.JAMA)
                    ? running.subtract(e.getAmount())
                    : running.add(e.getAmount());

            DairyDTOs.LedgerEntryResponse dto = new DairyDTOs.LedgerEntryResponse();
            dto.setId(e.getId());
            dto.setType(e.getType().name());
            dto.setAmount(e.getAmount());
            dto.setEntryDate(e.getEntryDate());
            dto.setBillFromDate(e.getBillFromDate());
            dto.setBillToDate(e.getBillToDate());
            dto.setMilkType(e.getMilkType());
            dto.setNote(e.getNote());
            dto.setBalanceAfter(running);
            entryDTOs.add(dto);
        }

        DairyDTOs.LedgerResponse response = new DairyDTOs.LedgerResponse();
        response.setFarmerNumber(farmer.getFarmerNumber());
        response.setFarmerName(farmer.getName());
        response.setAnimalType(farmer.getAnimalType() != null ? farmer.getAnimalType().name() : null);
        response.setCurrentBalance(running);
        response.setEntries(entryDTOs);
        return response;
    }

    /** Normalizes/validates an optional milk-type tag; blank input is treated as "not set". */
    private String normalizeMilkType(String milkType) {
        if (milkType == null || milkType.isBlank()) {
            return null;
        }
        String upper = milkType.trim().toUpperCase();
        if (!upper.equals("COW") && !upper.equals("BUFFALO") && !upper.equals("BOTH")) {
            throw new IllegalArgumentException(
                    "Invalid milkType '" + milkType + "'. Use COW, BUFFALO or BOTH.");
        }
        return upper;
    }

    private LedgerEntry.Type parseType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Type required: UCHAL, LAGAVAD or JAMA");
        }
        try {
            return LedgerEntry.Type.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid type '" + type + "'. Use UCHAL, LAGAVAD or JAMA.");
        }
    }
}
