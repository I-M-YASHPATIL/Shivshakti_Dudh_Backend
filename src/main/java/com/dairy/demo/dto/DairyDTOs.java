package com.dairy.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DairyDTOs {

    // ─── Branch ────────────────────────────────────────────────────────────────
    @Data
    public static class BranchDTO {
        private Long id;

        @NotBlank(message = "Branch code required (e.g. B1, B2)")
        private String code;

        @NotBlank(message = "Branch name required")
        private String name;

        private Boolean isActive;
    }

    // ─── Farmer ────────────────────────────────────────────────────────────────
    @Data
    public static class FarmerDTO {
        private Long id;

        private String branchCode;

        @NotNull(message = "Farmer number required")
        private Integer farmerNumber;

        @NotBlank(message = "Name required")
        private String name;

        private String phone;

        @NotBlank(message = "Animal type required: COW, BUFFALO or BOTH")
        private String animalType;  // "COW", "BUFFALO" or "BOTH"

        private Boolean isActive;
    }

    // ─── Ledger (उचल / लागवड / जमा) ────────────────────────────────────────────
    @Data
    public static class LedgerEntryRequest {
        @NotBlank(message = "Type required: UCHAL, LAGAVAD or JAMA")
        private String type;   // "UCHAL" | "LAGAVAD" | "JAMA"

        @NotNull(message = "Amount required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        private BigDecimal amount;

        @NotNull(message = "Date required")
        private LocalDate entryDate;

        // Optional: when a JAMA entry corresponds to a specific milk-bill period
        // (e.g. 01/09/2026 - 10/09/2026), the bill's period is stored for reference.
        private LocalDate billFromDate;
        private LocalDate billToDate;

        // Optional: for a JAMA entry belonging to a farmer who supplies BOTH
        // cow and buffalo milk, which section (COW / BUFFALO / BOTH) this
        // जमा should be counted against in the payment register.
        private String milkType;

        private String note;
    }

    @Data
    public static class LedgerEntryResponse {
        private Long id;
        private String type;
        private BigDecimal amount;
        private LocalDate entryDate;
        private LocalDate billFromDate;
        private LocalDate billToDate;
        private String milkType;
        private String note;
        private BigDecimal balanceAfter;
    }

    @Data
    public static class LedgerResponse {
        private Integer farmerNumber;
        private String farmerName;
        private String animalType;
        private BigDecimal currentBalance;
        private List<LedgerEntryResponse> entries;
    }

    // ─── Fat Rate ──────────────────────────────────────────────────────────────
    @Data
    public static class FatRateDTO {
        private Long id;

        private String branchCode;

        @NotNull
        private BigDecimal fatPercentage;

        private BigDecimal snf;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal ratePerLiter;

        @NotBlank(message = "Milk type required: COW or BUFFALO")
        private String milkType;  // "COW" or "BUFFALO"
    }

    @Data
    public static class MilkEntryRequest {
        @NotNull(message = "Farmer number required")
        private Integer farmerNumber;

        @NotNull(message = "Entry date required")
        private LocalDate entryDate;

        @NotBlank(message = "Session required: MORNING or EVENING")
        private String session;

        @NotBlank(message = "Milk type required: COW or BUFFALO")
        private String milkType;  // "COW" or "BUFFALO"

        @NotNull
        @DecimalMin("0.1")
        private BigDecimal liters;

        @NotNull
        private BigDecimal fat;

        private BigDecimal snf;
    }

    // ─── Milk Entry Response ───────────────────────────────────────────────────
    @Data
    public static class MilkEntryResponse {
        private Long id;
        private String branchCode;
        private Integer farmerNumber;
        private String farmerName;
        private LocalDate entryDate;
        private String session;
        private String milkType;
        private BigDecimal snf;// "COW" or "BUFFALO"
        private BigDecimal liters;
        private BigDecimal fat;
        private BigDecimal ratePerLiter;
        private BigDecimal amount;
    }

    // ─── Fat Lookup ────────────────────────────────────────────────────────────
    @Data
    public static class FatLookupRequest {
        @NotNull
        private BigDecimal fat;

        private BigDecimal liters;
        private BigDecimal snf;

        @NotBlank(message = "Milk type required: COW or BUFFALO")
        private String milkType;  // "COW" or "BUFFALO"
    }

    @Data
    public static class FatLookupResponse {
        private BigDecimal fat;
        private String milkType;         // "COW" or "BUFFALO"
        private BigDecimal ratePerLiter;
        private BigDecimal estimatedAmount;
    }

    // ─── Bill Generate Request ─────────────────────────────────────────────────
    @Data
    public static class BillGenerateRequest {
        @NotNull
        private LocalDate fromDate;

        @NotNull
        private LocalDate toDate;


        private BigDecimal savingPercent;       // e.g. 3.00 for 3%

        private BigDecimal advanceDeduction;    // आगाऊ वजावट
        private BigDecimal otherDeductions;     // इतर वजावट
    }

    // ─── Bill Response ─────────────────────────────────────────────────────────
    @Data
    public static class BillResponse {
        private Long id;
        private String branchCode;
        private Integer farmerNumber;
        private String farmerName;
        private String farmerVillage;
        private LocalDate fromDate;
        private LocalDate toDate;

        // Session totals
        private BigDecimal morningTotalLiters;
        private BigDecimal morningTotalAmount;
        private BigDecimal eveningTotalLiters;
        private BigDecimal eveningTotalAmount;

        // Milk type totals
        private BigDecimal cowTotalLiters;
        private BigDecimal cowTotalAmount;
        private BigDecimal buffaloTotalLiters;
        private BigDecimal buffaloTotalAmount;

        // Grand totals
        private BigDecimal totalLiters;
        private BigDecimal totalAmount;

        // Deductions
        private BigDecimal savingPercent;       // e.g. 3.00
        private BigDecimal savingDeduction;     // calculated saving amount deducted
        private BigDecimal sadilvar;            // fixed ₹5 per bill (सादिलवार वजावट)
        private BigDecimal advanceDeduction;    // आगाऊ वजावट
        private BigDecimal otherDeductions;     // इतर वजावट

        // Net payable
        // netAmount = totalAmount - savingDeduction - sadilvar - advanceDeduction - otherDeductions
        private BigDecimal netAmount;

        private Boolean isPaid;

        private List<MilkEntryResponse> entries;
    }

    // ─── Period Summary ────────────────────────────────────────────────────────
    @Data
    public static class PeriodSummaryResponse {
        private LocalDate fromDate;
        private LocalDate toDate;
        private int totalFarmers;
        private BigDecimal grandTotalLiters;
        private BigDecimal grandTotalAmount;
        private BigDecimal grandSavingDeduction;
        private BigDecimal grandSadilvar;       // total sadilvar across all bills
        private BigDecimal grandNetAmount;
        private BigDecimal morningTotalLiters;
        private BigDecimal eveningTotalLiters;
        private BigDecimal cowTotalLiters;
        private BigDecimal cowTotalAmount;
        private BigDecimal buffaloTotalLiters;
        private BigDecimal buffaloTotalAmount;
    }

    // ─── API Error ─────────────────────────────────────────────────────────────
    @Data
    public static class ApiError {
        private String message;
        private int status;
        private String timestamp;

        public ApiError(String message, int status) {
            this.message = message;
            this.status = status;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }
    }
}