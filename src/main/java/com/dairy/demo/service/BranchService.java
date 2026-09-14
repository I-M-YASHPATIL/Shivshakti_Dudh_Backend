package com.dairy.demo.service;

import com.dairy.demo.dto.DairyDTOs;
import com.dairy.demo.model.Branch;
import com.dairy.demo.repository.BranchRepository;
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
public class BranchService {

    private final BranchRepository branchRepository;

    public List<DairyDTOs.BranchDTO> getAllBranches() {
        return branchRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DairyDTOs.BranchDTO getBranchByCode(String code) {
        return toDTO(findBranchEntityByCode(code));
    }

    @Transactional
    public DairyDTOs.BranchDTO createBranch(DairyDTOs.BranchDTO dto) {
        String trimmedName = dto.getName().trim();

        if (branchRepository.existsByName(trimmedName)) {          // ← check name
            throw new IllegalArgumentException("Branch name already exists: " + trimmedName);
        }

        Branch branch = Branch.builder()
                .code(dto.getCode().toUpperCase().trim())
                .name(trimmedName)
                .isActive(true)
                .build();
        log.info("Creating branch: {} - {}", branch.getCode(), branch.getName());
        return toDTO(branchRepository.save(branch));
    }

    @Transactional
    public DairyDTOs.BranchDTO updateBranch(Long id, DairyDTOs.BranchDTO dto) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));

        String trimmedName = dto.getName().trim();

        // Allow same branch to keep its name, but block duplicates on others
        if (!branch.getName().equalsIgnoreCase(trimmedName)
                && branchRepository.existsByName(trimmedName)) {   // ← check on update too
            throw new IllegalArgumentException("Branch name already exists: " + trimmedName);
        }

        branch.setName(trimmedName);
        if (dto.getIsActive() != null) branch.setIsActive(dto.getIsActive());
        return toDTO(branchRepository.save(branch));
    }

    /** Used by other services to resolve branch entity from code */
    public Branch findBranchEntityByCode(String code) {
        return branchRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Branch not found: " + code));
    }

    /** Used by other services when they already have a branch id */
    public Branch findBranchEntityById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));
    }

    public DairyDTOs.BranchDTO toDTO(Branch branch) {
        DairyDTOs.BranchDTO dto = new DairyDTOs.BranchDTO();
        dto.setId(branch.getId());
        dto.setCode(branch.getCode());
        dto.setName(branch.getName());
        dto.setIsActive(branch.getIsActive());
        return dto;
    }
}