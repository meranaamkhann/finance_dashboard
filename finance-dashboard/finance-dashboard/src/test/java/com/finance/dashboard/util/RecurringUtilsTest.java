package com.finance.dashboard.util;

import com.finance.dashboard.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecurringUtils Unit Tests")
class RecurringUtilsTest {

    private static final LocalDate BASE = LocalDate.of(2024, 1, 15);

    @Test
    @DisplayName("nextExecutionDate — MONTHLY adds one month to lastExecutedDate")
    void nextExecution_Monthly() {
        RecurringTransaction rule = rule(RecurringFrequency.MONTHLY, BASE, BASE);
        assertThat(RecurringUtils.nextExecutionDate(rule)).isEqualTo(BASE.plusMonths(1));
    }

    @Test
    @DisplayName("nextExecutionDate — WEEKLY adds 7 days")
    void nextExecution_Weekly() {
        RecurringTransaction rule = rule(RecurringFrequency.WEEKLY, BASE, BASE);
        assertThat(RecurringUtils.nextExecutionDate(rule)).isEqualTo(BASE.plusWeeks(1));
    }

    @Test
    @DisplayName("nextExecutionDate — DAILY adds 1 day")
    void nextExecution_Daily() {
        RecurringTransaction rule = rule(RecurringFrequency.DAILY, BASE, BASE);
        assertThat(RecurringUtils.nextExecutionDate(rule)).isEqualTo(BASE.plusDays(1));
    }

    @Test
    @DisplayName("nextExecutionDate — QUARTERLY adds 3 months")
    void nextExecution_Quarterly() {
        RecurringTransaction rule = rule(RecurringFrequency.QUARTERLY, BASE, BASE);
        assertThat(RecurringUtils.nextExecutionDate(rule)).isEqualTo(BASE.plusMonths(3));
    }

    @Test
    @DisplayName("nextExecutionDate — YEARLY adds 1 year")
    void nextExecution_Yearly() {
        RecurringTransaction rule = rule(RecurringFrequency.YEARLY, BASE, BASE);
        assertThat(RecurringUtils.nextExecutionDate(rule)).isEqualTo(BASE.plusYears(1));
    }

    @Test
    @DisplayName("isDueToday — never-run rule whose startDate is today returns true")
    void isDue_NeverRun_StartDateIsToday() {
        LocalDate today = LocalDate.now();
        RecurringTransaction rule = rule(RecurringFrequency.MONTHLY, today, null);
        assertThat(RecurringUtils.isDueToday(rule, today)).isTrue();
    }

    @Test
    @DisplayName("isDueToday — inactive rule always returns false")
    void isDue_Inactive_ReturnsFalse() {
        LocalDate today = LocalDate.now();
        RecurringTransaction rule = rule(RecurringFrequency.DAILY, today.minusDays(1), today.minusDays(1));
        rule.setActive(false);
        assertThat(RecurringUtils.isDueToday(rule, today)).isFalse();
    }

    @Test
    @DisplayName("isDueToday — past endDate returns false")
    void isDue_PastEndDate_ReturnsFalse() {
        LocalDate today = LocalDate.now();
        RecurringTransaction rule = rule(RecurringFrequency.MONTHLY, today.minusMonths(2), today.minusMonths(1));
        rule.setEndDate(today.minusDays(1));
        assertThat(RecurringUtils.isDueToday(rule, today)).isFalse();
    }

    @Test
    @DisplayName("isDueToday — future startDate returns false")
    void isDue_FutureStart_ReturnsFalse() {
        LocalDate today = LocalDate.now();
        RecurringTransaction rule = rule(RecurringFrequency.MONTHLY, today.plusDays(5), null);
        assertThat(RecurringUtils.isDueToday(rule, today)).isFalse();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private RecurringTransaction rule(RecurringFrequency freq, LocalDate start, LocalDate lastRun) {
        User owner = User.builder().id(1L).username("u").email("u@u.com")
                .password("x").role(Role.ADMIN).active(true).build();
        return RecurringTransaction.builder()
                .id(1L).owner(owner).name("Test Rule")
                .amount(BigDecimal.TEN).type(TransactionType.EXPENSE)
                .category(Category.FOOD).frequency(freq)
                .startDate(start).lastExecutedDate(lastRun)
                .active(true).build();
    }
}
