package com.dairy.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "milk_entries",
        indexes = {
                @Index(name = "idx_milk_entry_date", columnList = "entry_date"),
                @Index(name = "idx_milk_entry_farmer_date", columnList = "farmer_id, entry_date"),
                @Index(name = "idx_milk_entry_farmer_date_session_type",
                        columnList = "farmer_id, entry_date, session, milk_type", unique = true)
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilkEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "milk_entry_seq")
    @SequenceGenerator(name = "milk_entry_seq", sequenceName = "milk_entry_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_milk_entry_farmer"))
    @NotNull(message = "Farmer is required")
    private Farmer farmer;

    @Column(name = "entry_date", nullable = false)
    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @Column(name = "session", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Session (MORNING/EVENING) is required")
    private Session session;

    @Column(name = "milk_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Milk type (COW/BUFFALO) is required")
    private MilkType milkType;

    @Column(name = "liters", nullable = false, precision = 6, scale = 2)
    @NotNull
    @DecimalMin(value = "0.1", message = "Liters must be positive")
    private BigDecimal liters;

    @Column(name = "fat", nullable = false, precision = 4, scale = 1)
    @NotNull
    private BigDecimal fat;

    private BigDecimal snf;

    @Column(name = "rate_per_liter", nullable = false, precision = 8, scale = 2)
    @NotNull
    private BigDecimal ratePerLiter;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal amount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Session {
        MORNING, EVENING
    }

    public enum MilkType {
        COW, BUFFALO
    }
}