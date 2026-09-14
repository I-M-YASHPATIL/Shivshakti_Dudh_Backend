package com.dairy.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "farmers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"branch_id", "farmer_number"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "farmer_number", nullable = false)
    private Integer farmerNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type", nullable = false)
    private AnimalType animalType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}