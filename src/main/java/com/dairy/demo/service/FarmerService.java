package com.dairy.demo.service;

import com.dairy.demo.dto.DairyDTOs;
import com.dairy.demo.model.AnimalType;
import com.dairy.demo.model.Branch;
import com.dairy.demo.model.Farmer;
import com.dairy.demo.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final BranchService branchService;

    public List<DairyDTOs.FarmerDTO> getAllFarmers(String branchCode) {
        Long branchId = branchService.findBranchEntityByCode(branchCode).getId();
        return farmerRepository.findByBranchIdAndIsActiveTrueOrderByFarmerNumberAsc(branchId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DairyDTOs.FarmerDTO> getAllFarmersIncludingInactive(String branchCode) {
        Long branchId = branchService.findBranchEntityByCode(branchCode).getId();
        return farmerRepository.findByBranchIdOrderByFarmerNumberAsc(branchId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DairyDTOs.FarmerDTO getFarmerByNumber(String branchCode, Integer farmerNumber) {
        return toDTO(findFarmerEntityByNumber(branchCode, farmerNumber));
    }

    @Transactional
    public DairyDTOs.FarmerDTO createFarmer(String branchCode, DairyDTOs.FarmerDTO dto) {
        Branch branch = branchService.findBranchEntityByCode(branchCode);

        if (farmerRepository.existsByBranchIdAndFarmerNumber(
                branch.getId(), dto.getFarmerNumber())) {
            throw new IllegalArgumentException(
                    "Farmer number " + dto.getFarmerNumber() +
                            " already exists in branch " + branchCode);
        }

        AnimalType animalType = parseAnimalType(dto.getAnimalType());

        Farmer farmer = Farmer.builder()
                .branch(branch)
                .farmerNumber(dto.getFarmerNumber())
                .name(dto.getName().trim())
                .phone(dto.getPhone())
                .animalType(animalType)
                .isActive(true)
                .build();

        log.info("Creating farmer: branch={}, #{} - {} ({})",
                branchCode, farmer.getFarmerNumber(), farmer.getName(), farmer.getAnimalType());
        return toDTO(farmerRepository.save(farmer));
    }

    @Transactional
    public DairyDTOs.FarmerDTO updateFarmer(Long id, DairyDTOs.FarmerDTO dto) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found with id: " + id));
        farmer.setName(dto.getName().trim());
        farmer.setPhone(dto.getPhone());
        if (dto.getAnimalType() != null) {
            farmer.setAnimalType(parseAnimalType(dto.getAnimalType()));
        }
        if (dto.getIsActive() != null) farmer.setIsActive(dto.getIsActive());
        log.info("Updating farmer: branch={}, #{}",
                farmer.getBranch().getCode(), farmer.getFarmerNumber());
        return toDTO(farmerRepository.save(farmer));
    }

    @Transactional
    public DairyDTOs.FarmerDTO deleteFarmer(String branchCode, Integer farmerNumber) {
        Farmer farmer = findFarmerEntityByNumber(branchCode, farmerNumber);
        farmer.setIsActive(false);
        log.info("Deactivating farmer: branch={}, #{} - {}",
                branchCode, farmer.getFarmerNumber(), farmer.getName());
        return toDTO(farmerRepository.save(farmer));
    }

    @Transactional
    public void hardDeleteFarmer(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found with id: " + id));
        log.info("Hard deleting farmer: branch={}, #{} - {}",
                farmer.getBranch().getCode(), farmer.getFarmerNumber(), farmer.getName());
        farmerRepository.delete(farmer);
    }

    public List<DairyDTOs.FarmerDTO> searchFarmers(String branchCode, String query) {
        Long branchId = branchService.findBranchEntityByCode(branchCode).getId();
        return farmerRepository.searchFarmers(branchId, query)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Farmer findFarmerEntityByNumber(String branchCode, Integer farmerNumber) {
        Long branchId = getBranchIdByCode(branchCode);
        return farmerRepository.findByBranchIdAndFarmerNumber(branchId, farmerNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Farmer #" + farmerNumber + " not found in branch " + branchCode));
    }

    public Long getBranchIdByCode(String branchCode) {
        return branchService.findBranchEntityByCode(branchCode).getId();
    }

    private AnimalType parseAnimalType(String animalType) {
        if (animalType == null) return AnimalType.COW;
        try {
            return AnimalType.valueOf(animalType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid animal type '" + animalType + "'. Use COW, BUFFALO or BOTH.");
        }
    }

    public DairyDTOs.FarmerDTO toDTO(Farmer farmer) {
        DairyDTOs.FarmerDTO dto = new DairyDTOs.FarmerDTO();
        dto.setId(farmer.getId());
        dto.setBranchCode(farmer.getBranch().getCode());
        dto.setFarmerNumber(farmer.getFarmerNumber());
        dto.setName(farmer.getName());
        dto.setPhone(farmer.getPhone());
        dto.setAnimalType(farmer.getAnimalType() != null ? farmer.getAnimalType().name() : null);
        dto.setIsActive(farmer.getIsActive());
        return dto;
    }
}