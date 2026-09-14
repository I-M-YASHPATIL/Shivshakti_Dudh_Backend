package com.dairy.demo.service;

import com.dairy.demo.dto.DairyDTOs;
import com.dairy.demo.model.Branch;
import com.dairy.demo.model.FatRate;
import com.dairy.demo.model.MilkEntry;
import com.dairy.demo.repository.FatRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FatRateService {

    private static final Set<BigDecimal> COW_SNF_VALUES = Set.of(
            new BigDecimal("8.6"), new BigDecimal("8.3"),
            new BigDecimal("8.4"), new BigDecimal("8.5")
    );

    private static final Set<BigDecimal> BUFFALO_SNF_VALUES = Set.of(
            new BigDecimal("8.8"), new BigDecimal("8.9"), new BigDecimal("9.0"),
            new BigDecimal("9.1"), new BigDecimal("9.2"), new BigDecimal("9.3")
    );

    private final FatRateRepository fatRateRepository;
    private final BranchService branchService;

    public List<DairyDTOs.FatRateDTO> getAllRates(String branchCode) {
        Long branchId = branchService.findBranchEntityByCode(branchCode).getId();
        return fatRateRepository.findByBranchIdOrderByFatPercentageAsc(branchId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DairyDTOs.FatRateDTO> getRatesByMilkType(String branchCode, String milkType) {
        Long branchId = branchService.findBranchEntityByCode(branchCode).getId();
        MilkEntry.MilkType type = parseMilkType(milkType);
        return fatRateRepository.findByBranchIdAndMilkTypeOrderByFatPercentageAsc(branchId, type)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public DairyDTOs.FatRateDTO saveRate(String branchCode, DairyDTOs.FatRateDTO dto) {
        Branch branch = branchService.findBranchEntityByCode(branchCode);
        MilkEntry.MilkType type = parseMilkType(dto.getMilkType());
        BigDecimal snf = validateSnf(type, dto.getSnf());

        FatRate fatRate = fatRateRepository
                .findByBranchIdAndFatPercentageAndMilkTypeAndSnf(
                        branch.getId(), dto.getFatPercentage(), type, snf)
                .orElse(new FatRate());

        fatRate.setBranch(branch);
        fatRate.setFatPercentage(dto.getFatPercentage());
        fatRate.setSnf(snf);
        fatRate.setRatePerLiter(dto.getRatePerLiter());
        fatRate.setMilkType(type);

        FatRate saved = fatRateRepository.save(fatRate);
        log.info("Fat rate saved: branch={}, fat={}%, snf={}, type={}, rate={}",
                branchCode, saved.getFatPercentage(), saved.getSnf(), saved.getMilkType(), saved.getRatePerLiter());
        return toDTO(saved);
    }

    @Transactional
    public DairyDTOs.FatRateDTO updateRate(String branchCode, Long id, DairyDTOs.FatRateDTO dto) {
        Branch branch = branchService.findBranchEntityByCode(branchCode);
        MilkEntry.MilkType type = parseMilkType(dto.getMilkType());
        BigDecimal snf = validateSnf(type, dto.getSnf());

        FatRate fatRate = fatRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fat rate not found with id: " + id));

        if (!fatRate.getBranch().getId().equals(branch.getId())) {
            throw new IllegalArgumentException(
                    "Fat rate id=" + id + " does not belong to branch " + branchCode);
        }

        fatRate.setFatPercentage(dto.getFatPercentage());
        fatRate.setSnf(snf);
        fatRate.setRatePerLiter(dto.getRatePerLiter());
        fatRate.setMilkType(type);

        FatRate updated = fatRateRepository.save(fatRate);
        log.info("Fat rate updated: id={}, branch={}, fat={}%, snf={}, type={}, rate={}",
                id, branchCode, updated.getFatPercentage(), updated.getSnf(), updated.getMilkType(), updated.getRatePerLiter());
        return toDTO(updated);
    }

    @Transactional
    public void deleteRate(Long id) {
        if (!fatRateRepository.existsById(id)) {
            throw new RuntimeException("Fat rate not found with id: " + id);
        }
        fatRateRepository.deleteById(id);
        log.info("Fat rate deleted: id={}", id);
    }

    private MilkEntry.MilkType parseMilkType(String milkType) {
        try {
            return MilkEntry.MilkType.valueOf(milkType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid milk type '" + milkType + "'. Use COW or BUFFALO.");
        }
    }

    private BigDecimal validateSnf(MilkEntry.MilkType type, BigDecimal snf) {
        if (snf == null) {
            return null;
        }
        Set<BigDecimal> allowed = (type == MilkEntry.MilkType.BUFFALO) ? BUFFALO_SNF_VALUES : COW_SNF_VALUES;
        boolean valid = allowed.stream().anyMatch(v -> v.compareTo(snf) == 0);
        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid SNF '" + snf + "' for milk type " + type +
                            ". Allowed values: " + allowed.stream()
                            .sorted()
                            .map(BigDecimal::toPlainString)
                            .collect(Collectors.joining(", ")));
        }
        return snf;
    }

    public DairyDTOs.FatRateDTO toDTO(FatRate fatRate) {
        DairyDTOs.FatRateDTO dto = new DairyDTOs.FatRateDTO();
        dto.setId(fatRate.getId());
        dto.setBranchCode(fatRate.getBranch().getCode());
        dto.setFatPercentage(fatRate.getFatPercentage());
        dto.setSnf(fatRate.getSnf());
        dto.setRatePerLiter(fatRate.getRatePerLiter());
        dto.setMilkType(fatRate.getMilkType().name());
        return dto;
    }
}