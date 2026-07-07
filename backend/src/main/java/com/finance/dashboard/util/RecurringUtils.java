package com.finance.dashboard.util;
import com.finance.dashboard.model.RecurringTransaction;
import com.finance.dashboard.model.enums.RecurringFrequency;
import java.time.LocalDate;
import java.util.function.IntPredicate;

public final class RecurringUtils {
    private RecurringUtils() {}
    public static LocalDate computeNextDate(LocalDate base, RecurringFrequency freq) {
        return switch (freq) {
            case DAILY     -> base.plusDays(1);
            case WEEKLY    -> base.plusWeeks(1);
            case BIWEEKLY  -> base.plusWeeks(2);
            case MONTHLY   -> base.plusMonths(1);
            case QUARTERLY -> base.plusMonths(3);
            case YEARLY    -> base.plusYears(1);
        };
    }
    public static LocalDate initialNextDate(LocalDate startDate, RecurringFrequency freq) {
        LocalDate today = LocalDate.now();
        if (!startDate.isBefore(today)) return startDate;
        LocalDate next = startDate;
        while (next.isBefore(today)) next = computeNextDate(next, freq);
        return next;
    }
    public static LocalDate nextExecutionDate(RecurringTransaction rule) {
    LocalDate base = rule.getLastExecutedDate() != null
            ? rule.getLastExecutedDate()
            : rule.getStartDate();

    return computeNextDate(base, rule.getFrequency());
}

public static IntPredicate isDueToday(RecurringTransaction rule, LocalDate today) {
    return ignored -> {
        if (!rule.isActive()) return false;

        if (rule.getStartDate().isAfter(today)) return false;

        if (rule.getEndDate() != null && rule.getEndDate().isBefore(today))
            return false;

        LocalDate next = rule.getLastExecutedDate() == null
                ? rule.getStartDate()
                : nextExecutionDate(rule);

        return next.equals(today);
    };
}
}
