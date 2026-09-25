package com.dairy.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "lagwad_types",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "name"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LagwadType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "unit", length = 30)
    private String unit;
}