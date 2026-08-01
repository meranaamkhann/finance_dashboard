package com.finance.dashboard.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(nullable = false)
    @Builder.Default
    private int maxRecords = 100;

    @Column(nullable = false)
    @Builder.Default
    private int maxBudgets = 3;

    @Column(nullable = false)
    @Builder.Default
    private int maxRecurring = 3;

    @Column(nullable = false)
    @Builder.Default
    private int maxExports = 5;

    @Column(nullable = false)
    @Builder.Default
    private int maxUsers = 1;

    @Column(nullable = false)
    @Builder.Default
    private boolean apiAccess = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean prioritySupport = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean visible = true;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @ElementCollection
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature", length = 200)
    private List<String> features;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}