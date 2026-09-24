package com.dairy.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bills",
        indexes = {
                @Index(name = "idx_bill_farmer", columnList = "farmer_id"),
                @Index(name = "idx_bill_dates", columnList = "from_date, to_date"),
                @Index(name = "idx_bill_farmer_dates",
                        columnList = "farmer_id, from_date, to_date", unique = true)
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_seq")
    @SequenceGenerator(name = "bill_seq", sequenceName = "bill_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_bill_farmer"))
    private Farmer farmer;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "morning_total_liters", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal morningTotalLiters = BigDecimal.ZERO;

    @Column(name = "morning_total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal morningTotalAmount = BigDecimal.ZERO;

    @Column(name = "evening_total_liters", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal eveningTotalLiters = BigDecimal.ZERO;

    @Column(name = "evening_total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal eveningTotalAmount = BigDecimal.ZERO;

    @Column(name = "cow_total_liters", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal cowTotalLiters = BigDecimal.ZERO;

    @Column(name = "cow_total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cowTotalAmount = BigDecimal.ZERO;

    @Column(name = "buffalo_total_liters", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal buffaloTotalLiters = BigDecimal.ZERO;

    @Column(name = "buffalo_total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal buffaloTotalAmount = BigDecimal.ZERO;

    @Column(name = "total_liters", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalLiters = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "saving_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal savingPercent = new BigDecimal("3.00");

    @Column(name = "saving_deduction", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal savingDeduction = BigDecimal.ZERO;

    @Column(name = "sadilvar", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sadilvar = new BigDecimal("6.00");

    @Column(name = "advance_deduction", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal advanceDeduction = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;


    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean isPaid = false;

    @Column(name = "generated_date")
    private LocalDate generatedDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}