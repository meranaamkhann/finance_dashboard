package com.finance.dashboard.util;
import com.finance.dashboard.model.enums.RecurringFrequency;
import java.time.LocalDate;

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
}
