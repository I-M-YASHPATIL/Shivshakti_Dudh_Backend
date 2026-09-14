package com.dairy.demo.service;

import com.dairy.demo.dto.DairyDTOs;
import com.dairy.demo.model.Farmer;
import com.dairy.demo.model.FatRate;
import com.dairy.demo.model.MilkEntry;
import com.dairy.demo.repository.FatRateRepository;
import com.dairy.demo.repository.MilkEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MilkEntryService {

    private final MilkEntryRepository milkEntryRepository;
    private final FatRateRepository   fatRateRepository;
    private final FarmerService       farmerService;


    public DairyDTOs.FatLookupResponse lookupRate(
            String branchCode, DairyDTOs.FatLookupRequest request) {

        Long branchId = farmerService.getBranchIdByCode(branchCode);
        MilkEntry.MilkType milkType = parseMilkType(request.getMilkType());
        validateFat(request.getFat(), milkType);

        FatRate fatRate = fatRateRepository
                .findByBranchIdAndFatPercentageAndMilkTypeAndSnf(
                        branchId, request.getFat(), milkType, request.getSnf())
                .orElseThrow(() -> new RuntimeException(
                        "No rate found for fat: " + request.getFat() + "%, snf: " + request.getSnf() +
                                " (" + milkType + " milk) in branch " + branchCode +
                                ". Please add it in Fat Rates settings."));

        DairyDTOs.FatLookupResponse response = new DairyDTOs.FatLookupResponse();
        response.setFat(request.getFat());
        response.setMilkType(milkType.name());
        response.setRatePerLiter(fatRate.getRatePerLiter());

        if (request.getLiters() != null &&
                request.getLiters().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amount = request.getLiters()
                    .multiply(fatRate.getRatePerLiter())
                    .setScale(2, RoundingMode.HALF_UP);
            response.setEstimatedAmount(amount);
        }
        return response;
    }


    @Transactional
    public DairyDTOs.MilkEntryResponse createEntry(
            String branchCode, DairyDTOs.MilkEntryRequest request) {

        Farmer farmer = farmerService.findFarmerEntityByNumber(
                branchCode, request.getFarmerNumber());

        MilkEntry.Session  session  = parseSession(request.getSession());
        MilkEntry.MilkType milkType = resolveMilkType(farmer, request.getMilkType());

        validateFat(request.getFat(), milkType);

        // Duplicate check
        if (milkEntryRepository.existsByFarmerIdAndEntryDateAndSessionAndMilkType(
                farmer.getId(), request.getEntryDate(), session, milkType)) {
            throw new IllegalStateException(
                    "Entry already exists for farmer #" + request.getFarmerNumber() +
                            " (" + farmer.getName() + ") on " + request.getEntryDate() +
                            " - " + session + " session - " + milkType + " milk" +
                            " [branch: " + branchCode + "]");
        }

        Long branchId = farmer.getBranch().getId();
        FatRate fatRate = fatRateRepository
                .findByBranchIdAndFatPercentageAndMilkTypeAndSnf(
                        branchId, request.getFat(), milkType, request.getSnf())
                .orElseThrow(() -> new RuntimeException(
                        "No rate configured for fat " + request.getFat() + "%, snf " + request.getSnf() +
                                " (" + milkType + " milk) in branch " + branchCode + "."));

        BigDecimal amount = request.getLiters()
                .multiply(fatRate.getRatePerLiter())
                .setScale(2, RoundingMode.HALF_UP);

        MilkEntry entry = MilkEntry.builder()
                .farmer(farmer)
                .entryDate(request.getEntryDate())
                .session(session)
                .milkType(milkType)
                .liters(request.getLiters())
                .fat(request.getFat())
                .snf(request.getSnf())
                .ratePerLiter(fatRate.getRatePerLiter())
                .amount(amount)
                .build();

        MilkEntry saved = milkEntryRepository.save(entry);
        log.info("Milk entry saved: branch={}, farmer={}, date={}, session={}, " +
                        "milkType={}, liters={}, fat={}, snf={}, amount={}",
                branchCode, farmer.getFarmerNumber(), request.getEntryDate(), session,
                milkType, request.getLiters(), request.getFat(), request.getSnf(), amount);
        return toDTO(saved);
    }


    public List<DairyDTOs.MilkEntryResponse> getEntriesByDate(
            String branchCode, LocalDate date) {
        Long branchId = farmerService.getBranchIdByCode(branchCode);
        return milkEntryRepository
                .findByFarmerBranchIdAndEntryDateOrderByFarmerFarmerNumberAscSessionAsc(
                        branchId, date)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DairyDTOs.MilkEntryResponse> getEntriesByDateAndSession(
            String branchCode, LocalDate date, MilkEntry.Session session) {
        Long branchId = farmerService.getBranchIdByCode(branchCode);
        return milkEntryRepository
                .findByFarmerBranchIdAndEntryDateAndSessionOrderByFarmerFarmerNumberAsc(
                        branchId, date, session)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }


    public List<DairyDTOs.MilkEntryResponse> getEntriesByDateSessionAndMilkType(
            String branchCode, LocalDate date,
            MilkEntry.Session session,
            MilkEntry.MilkType milkType) {
        Long branchId = farmerService.getBranchIdByCode(branchCode);
        return milkEntryRepository
                .findByBranchDateSessionMilkType(branchId, date, session, milkType)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DairyDTOs.MilkEntryResponse> getEntriesByFarmerAndDateRange(
            String branchCode, Integer farmerNumber, LocalDate from, LocalDate to) {
        Farmer farmer = farmerService.findFarmerEntityByNumber(branchCode, farmerNumber);
        return milkEntryRepository
                .findByFarmerIdAndEntryDateBetweenOrderByEntryDateAscSessionAsc(
                        farmer.getId(), from, to)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DairyDTOs.MilkEntryResponse> getEntriesByDateRange(
            String branchCode, LocalDate from, LocalDate to) {
        Long branchId = farmerService.getBranchIdByCode(branchCode);
        return milkEntryRepository
                .findByFarmerBranchIdAndEntryDateBetweenOrderByFarmerFarmerNumberAscEntryDateAscSessionAsc(
                        branchId, from, to)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DairyDTOs.MilkEntryResponse> getYearlyEntriesForFarmer(
            String branchCode, Integer farmerNumber, LocalDate from, LocalDate to) {

        Long branchId = farmerService.getBranchIdByCode(branchCode);
        log.info("Fetching yearly entries: branch={}, farmer={}, from={}, to={}",
                branchCode, farmerNumber, from, to);

        return milkEntryRepository
                .findByBranchAndFarmerNumberAndDateRange(branchId, farmerNumber, from, to)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<DairyDTOs.MilkEntryResponse> getYearlyEntriesForFarmerById(
            String branchCode, Long farmerId, LocalDate from, LocalDate to) {

        Long branchId = farmerService.getBranchIdByCode(branchCode);
        log.info("Fetching yearly entries by id: branch={}, farmerId={}, from={}, to={}",
                branchCode, farmerId, from, to);

        return milkEntryRepository
                .findByBranchIdAndFarmerIdAndDateRange(branchId, farmerId, from, to)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteEntry(Long id) {
        if (!milkEntryRepository.existsById(id)) {
            throw new RuntimeException("Milk entry not found with id: " + id);
        }
        milkEntryRepository.deleteById(id);
        log.info("Milk entry deleted: id={}", id);
    }


    /**
     * Farmers registered as COW or BUFFALO are always locked to that single type.
     * Farmers registered as BOTH supply either milk type depending on the visit,
     * so for them we take the milk type from the request instead.
     */
    private MilkEntry.MilkType resolveMilkType(Farmer farmer, String requestedMilkType) {
        switch (farmer.getAnimalType()) {
            case COW:
                return MilkEntry.MilkType.COW;
            case BUFFALO:
                return MilkEntry.MilkType.BUFFALO;
            case BOTH:
                if (requestedMilkType == null || requestedMilkType.isBlank()) {
                    throw new IllegalArgumentException(
                            "Farmer " + farmer.getName() + " supplies both COW and BUFFALO milk - " +
                                    "please specify milkType for this entry.");
                }
                return parseMilkType(requestedMilkType);
            default:
                throw new IllegalStateException("Unknown animal type: " + farmer.getAnimalType());
        }
    }

    private void validateFat(BigDecimal fat, MilkEntry.MilkType milkType) {
        if (fat == null) throw new IllegalArgumentException("Fat value is required.");
        if (milkType == MilkEntry.MilkType.COW) {
            if (fat.compareTo(new BigDecimal("2.8")) < 0 ||
                    fat.compareTo(new BigDecimal("5.0")) > 0) {
                throw new IllegalArgumentException(
                        "गाय फॅट 2.8% ते 5.0% च्या दरम्यान असणे आवश्यक आहे. दिलेला फॅट: " + fat + "%");
            }
        } else if (milkType == MilkEntry.MilkType.BUFFALO) {
            if (fat.compareTo(new BigDecimal("4.8")) < 0 ||
                    fat.compareTo(new BigDecimal("12.0")) > 0) {
                throw new IllegalArgumentException(
                        "म्हैस फॅट 4.8% ते 12.0% च्या दरम्यान असणे आवश्यक आहे. दिलेला फॅट: " + fat + "%");
            }
        }
    }

    private MilkEntry.Session parseSession(String session) {
        try {
            return MilkEntry.Session.valueOf(session.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid session '" + session + "'. Use MORNING or EVENING.");
        }
    }

    private MilkEntry.MilkType parseMilkType(String milkType) {
        try {
            return MilkEntry.MilkType.valueOf(milkType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid milk type '" + milkType + "'. Use COW or BUFFALO.");
        }
    }

    public DairyDTOs.MilkEntryResponse toDTO(MilkEntry entry) {
        DairyDTOs.MilkEntryResponse dto = new DairyDTOs.MilkEntryResponse();
        dto.setId(entry.getId());
        dto.setBranchCode(entry.getFarmer().getBranch().getCode());
        dto.setFarmerNumber(entry.getFarmer().getFarmerNumber());
        dto.setFarmerName(entry.getFarmer().getName());
        dto.setEntryDate(entry.getEntryDate());
        dto.setSession(entry.getSession().name());
        dto.setMilkType(entry.getMilkType().name());
        dto.setLiters(entry.getLiters());
        dto.setFat(entry.getFat());
        dto.setSnf(entry.getSnf());
        dto.setRatePerLiter(entry.getRatePerLiter());
        dto.setAmount(entry.getAmount());
        return dto;
    }
}