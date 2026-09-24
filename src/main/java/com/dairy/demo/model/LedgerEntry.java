package com.dairy.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_farmer", columnList = "farmer_id"),
                @Index(name = "idx_ledger_farmer_date", columnList = "farmer_id, entry_date")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ledger_entry_seq")
    @SequenceGenerator(name = "ledger_entry_seq", sequenceName = "ledger_entry_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ledger_farmer"))
    private Farmer farmer;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private Type type;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "bill_from_date")
    private LocalDate billFromDate;

    @Column(name = "bill_to_date")
    private LocalDate billToDate;

    @Column(name = "milk_type", length = 10)
    private String milkType; 

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Type {
        UCHAL,    
        LAGAVAD,  
        JAMA     
    }
}
