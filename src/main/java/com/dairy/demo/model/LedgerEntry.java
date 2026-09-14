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

    // Only set for JAMA entries that are tied to a specific milk bill period
    @Column(name = "bill_from_date")
    private LocalDate billFromDate;

    @Column(name = "bill_to_date")
    private LocalDate billToDate;

    // Only meaningful for JAMA entries of a farmer who supplies BOTH cow and
    // buffalo milk: tells the payment register which section (गाय / म्हैस)
    // this जमा should be counted against, instead of splitting it
    // proportionally. Null/omitted for farmers who supply only one type,
    // or for legacy entries recorded before this field existed.
    @Column(name = "milk_type", length = 10)
    private String milkType;   // "COW" | "BUFFALO" | "BOTH" | null

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Type {
        UCHAL,    // उचल  - advance drawn by the farmer, increases बाकी
        LAGAVAD,  // लागवड - another advance type, also increases बाकी
        JAMA      // जमा  - credit/repayment, decreases बाकी
    }
}
