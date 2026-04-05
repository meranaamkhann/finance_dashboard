package com.finance.dashboard.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_records", indexes = {
    @Index(name = "idx_record_date",     columnList = "date"),
    @Index(name = "idx_record_type",     columnList = "type"),
    @Index(name = "idx_record_category", columnList = "category"),
    @Index(name = "idx_record_deleted",  columnList = "deleted")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String description;

    /** Optional free-form tags, stored as comma-separated e.g. "q1,reimbursable" */
    @Column(length = 255)
    private String tags;

    /** Links back to the recurring rule that auto-generated this record, if any. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_rule_id")
    private RecurringTransaction recurringRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** Soft-delete flag — records are never physically removed. */
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
