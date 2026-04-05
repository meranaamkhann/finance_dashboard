package com.finance.dashboard.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A budget cap set by a user for a specific category and period.
 * The system evaluates spending against this limit and fires alerts
 * when configurable thresholds (warning / critical) are crossed.
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uq_budget_owner_category_period",
                      columnNames = {"owner_id", "category", "period_start", "period_end"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    /** Human-readable note, e.g. "Keep food costs lean this quarter". */
    @Column(length = 300)
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp                               private LocalDateTime updatedAt;
}
