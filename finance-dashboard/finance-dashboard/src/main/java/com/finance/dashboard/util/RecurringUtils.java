package com.finance.dashboard.util;

import com.finance.dashboard.model.RecurringFrequency;
import com.finance.dashboard.model.RecurringTransaction;

import java.time.LocalDate;

public final class RecurringUtils {

    private RecurringUtils() {}

    /**
     * Computes the next execution date given the rule's frequency and last-executed date.
     * Falls back to startDate when the rule has never run.
     */
    public static LocalDate nextExecutionDate(RecurringTransaction rule) {
        LocalDate base = rule.getLastExecutedDate() != null
                ? rule.getLastExecutedDate()
                : rule.getStartDate().minusDays(1); // ensure startDate itself is a candidate

        return switch (rule.getFrequency()) {
            case DAILY     -> base.plusDays(1);
            case WEEKLY    -> base.plusWeeks(1);
            case MONTHLY   -> base.plusMonths(1);
            case QUARTERLY -> base.plusMonths(3);
            case YEARLY    -> base.plusYears(1);
        };
    }

    /**
     * Returns true when a rule should fire today.
     * Handles first-run (lastExecutedDate == null) by checking startDate <= today.
     */
    public static boolean isDueToday(RecurringTransaction rule, LocalDate today) {
        if (!rule.isActive()) return false;
        if (rule.getStartDate().isAfter(today)) return false;
        if (rule.getEndDate() != null && rule.getEndDate().isBefore(today)) return false;

        if (rule.getLastExecutedDate() == null) {
            return !rule.getStartDate().isAfter(today);
        }

        LocalDate next = nextExecutionDate(rule);
        return !next.isAfter(today);
    }
}
