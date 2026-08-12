package com.finance.dashboard.repository;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.enums.Category;
import com.finance.dashboard.model.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class FinancialRecordSpecification {
    private FinancialRecordSpecification() {}
    public static Specification<FinancialRecord> filter(
            TransactionType type, Category category,
            LocalDate from, LocalDate to,
            String keyword, String tags, Long workspaceId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleted")));

            if (workspaceId != null) {
                predicates.add(cb.equal(root.get("workspaceId"), workspaceId));
            }
            if (type != null)
                predicates.add(cb.equal(root.get("type"), type));
            if (category != null)
                predicates.add(cb.equal(root.get("category"), category));
            if (from != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            if (to != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("description")), like));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
