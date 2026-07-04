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
    public static Specification<FinancialRecord> filter(TransactionType type, Category category, LocalDate from, LocalDate to, String keyword, String tags, Long userId) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isFalse(root.get("deleted")));
            if (type != null)     p.add(cb.equal(root.get("type"), type));
            if (category != null) p.add(cb.equal(root.get("category"), category));
            if (from != null)     p.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            if (to != null)       p.add(cb.lessThanOrEqualTo(root.get("date"), to));
            if (keyword != null && !keyword.isBlank()) p.add(cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%"));
            if (tags != null && !tags.isBlank())       p.add(cb.like(cb.lower(root.get("tags")), "%" + tags.toLowerCase() + "%"));
            if (userId != null)   p.add(cb.equal(root.get("createdBy").get("id"), userId));
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
