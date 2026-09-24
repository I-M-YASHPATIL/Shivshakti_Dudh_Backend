package com.dairy.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "fat_rates",
        uniqueConstraints = {

                @UniqueConstraint(columnNames = {"branch_id", "fat_percentage", "snf", "milk_type"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FatRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "fat_percentage", nullable = false, precision = 4, scale = 1)
    private BigDecimal fatPercentage;

    @Column(name = "snf", precision = 3, scale = 1)
    private BigDecimal snf;

    @Enumerated(EnumType.STRING)
    @Column(name = "milk_type", nullable = false)
    private MilkEntry.MilkType milkType;

    @Column(name = "rate_per_liter", nullable = false, precision = 8, scale = 2)
    private BigDecimal ratePerLiter;
}