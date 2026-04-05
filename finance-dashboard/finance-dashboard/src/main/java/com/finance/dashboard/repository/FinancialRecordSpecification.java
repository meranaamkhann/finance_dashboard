package com.finance.dashboard.repository;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe JPA Specification builder for FinancialRecord queries.
 * Every parameter is optional — only non-null values produce predicates.
 * The soft-delete filter is always applied.
 */
public final class FinancialRecordSpecification {

    private FinancialRecordSpecification() {}

    public static Specification<FinancialRecord> buildFilter(
            TransactionType type,
            Category category,
            LocalDate dateFrom,
            LocalDate dateTo,
            String keyword,
            String tags,
            Long createdById
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted
            predicates.add(cb.isFalse(root.get("deleted")));

            if (type       != null) predicates.add(cb.equal(root.get("type"), type));
            if (category   != null) predicates.add(cb.equal(root.get("category"), category));
            if (dateFrom   != null) predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
            if (dateTo     != null) predicates.add(cb.lessThanOrEqualTo(root.get("date"), dateTo));
            if (createdById!= null) predicates.add(cb.equal(root.get("createdBy").get("id"), createdById));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            }
            if (tags != null && !tags.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tags")),
                        "%" + tags.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
