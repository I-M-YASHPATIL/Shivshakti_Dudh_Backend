package com.dairy.demo.controller;

import com.dairy.demo.model.Branch;
import com.dairy.demo.model.LagwadType;
import com.dairy.demo.repository.LagwadTypeRepository;
import com.dairy.demo.service.BranchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/{branchCode}/lagwad-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class LagwadTypeController {

    private final LagwadTypeRepository lagwadTypeRepository;
    private final BranchService        branchService;

    @Data
    public static class LagwadTypeDTO {
        private Long id;

        @NotBlank(message = "लागवड प्रकाराचे नाव टाका")
        private String name;

        @NotNull(message = "किंमत टाका")
        @DecimalMin(value = "0.01", message = "किंमत 0 पेक्षा जास्त हवी")
        private BigDecimal price;

        private String unit;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<LagwadTypeDTO>> getAll(@PathVariable String branchCode) {
        Long branchId = branchService.findBranchEntityByCode(branchCode).getId();
        return ResponseEntity.ok(
                lagwadTypeRepository.findByBranchIdOrderByNameAsc(branchId)
                        .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<LagwadTypeDTO> create(
            @PathVariable String branchCode,
            @Valid @RequestBody LagwadTypeDTO dto) {
        Branch branch = branchService.findBranchEntityByCode(branchCode);
        String name = dto.getName().trim();

        if (lagwadTypeRepository.existsByBranchIdAndNameIgnoreCase(branch.getId(), name)) {
            throw new IllegalStateException("हा लागवड प्रकार आधीपासून आहे: " + name);
        }

        LagwadType saved = lagwadTypeRepository.save(LagwadType.builder()
                .branch(branch)
                .name(name)
                .price(dto.getPrice())
                .unit(cleanUnit(dto.getUnit()))
                .build());
        return ResponseEntity.ok(toDTO(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<LagwadTypeDTO> update(
            @PathVariable String branchCode,
            @PathVariable Long id,
            @Valid @RequestBody LagwadTypeDTO dto) {
        Branch branch = branchService.findBranchEntityByCode(branchCode);
        LagwadType lt = lagwadTypeRepository.findById(id)
                .filter(x -> x.getBranch().getId().equals(branch.getId()))
                .orElseThrow(() -> new RuntimeException("लागवड प्रकार सापडला नाही"));

        String name = dto.getName().trim();
        if (lagwadTypeRepository.existsByBranchIdAndNameIgnoreCaseAndIdNot(branch.getId(), name, id)) {
            throw new IllegalStateException("हा लागवड प्रकार आधीपासून आहे: " + name);
        }

        lt.setName(name);
        lt.setPrice(dto.getPrice());
        lt.setUnit(cleanUnit(dto.getUnit()));
        return ResponseEntity.ok(toDTO(lagwadTypeRepository.save(lt)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable String branchCode,
            @PathVariable Long id) {
        Branch branch = branchService.findBranchEntityByCode(branchCode);
        LagwadType lt = lagwadTypeRepository.findById(id)
                .filter(x -> x.getBranch().getId().equals(branch.getId()))
                .orElseThrow(() -> new RuntimeException("लागवड प्रकार सापडला नाही"));
        lagwadTypeRepository.delete(lt);
        return ResponseEntity.ok().build();
    }

    private String cleanUnit(String unit) {
        return (unit == null || unit.isBlank()) ? null : unit.trim();
    }

    private LagwadTypeDTO toDTO(LagwadType lt) {
        LagwadTypeDTO d = new LagwadTypeDTO();
        d.setId(lt.getId());
        d.setName(lt.getName());
        d.setPrice(lt.getPrice());
        d.setUnit(lt.getUnit());
        return d;
    }
}