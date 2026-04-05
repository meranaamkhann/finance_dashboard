package com.finance.dashboard.config;

import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository                userRepo;
    private final FinancialRecordRepository     recordRepo;
    private final BudgetRepository             budgetRepo;
    private final RecurringTransactionRepository recurringRepo;
    private final PasswordEncoder               passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return;
        User admin   = seedUsers();
        seedRecords(admin);
        seedBudgets(admin);
        seedRecurring(admin);
        log.info("✅  Seed data loaded — users: {}, records: {}, budgets: {}, rules: {}",
                userRepo.count(), recordRepo.count(), budgetRepo.count(), recurringRepo.count());
    }

    private User seedUsers() {
        User admin = userRepo.save(User.builder()
                .username("admin").email("admin@finance.dev")
                .password(passwordEncoder.encode("Admin@1234"))
                .fullName("System Administrator").role(Role.ADMIN).build());

        userRepo.save(User.builder()
                .username("analyst").email("analyst@finance.dev")
                .password(passwordEncoder.encode("Analyst@1234"))
                .fullName("Finance Analyst").role(Role.ANALYST).build());

        userRepo.save(User.builder()
                .username("viewer").email("viewer@finance.dev")
                .password(passwordEncoder.encode("Viewer@1234"))
                .fullName("Dashboard Viewer").role(Role.VIEWER).build());

        log.info("Seeded 3 default users (admin / analyst / viewer)");
        return admin;
    }

    private void seedRecords(User admin) {
        LocalDate now = LocalDate.now();
        // Current month income
        rec(admin, "85000", INCOME, Category.SALARY,         now.withDayOfMonth(1),           "April salary");
        rec(admin, "12000", INCOME, Category.FREELANCE,      now.minusDays(5),                 "Consulting — TechCorp");
        rec(admin, "4800",  INCOME, Category.INVESTMENT,     now.minusDays(10),                "Dividend payout");
        rec(admin, "18000", INCOME, Category.RENTAL,         now.minusDays(3),                 "Apartment rent received");
        // Current month expenses
        rec(admin, "25000", EXPENSE, Category.HOUSING,       now.withDayOfMonth(2),            "Monthly rent");
        rec(admin, "8200",  EXPENSE, Category.FOOD,          now.minusDays(2),                 "Groceries + dining out","food,monthly");
        rec(admin, "3100",  EXPENSE, Category.TRANSPORTATION,now.minusDays(4),                 "Fuel + Metro","transport");
        rec(admin, "2400",  EXPENSE, Category.UTILITIES,     now.minusDays(6),                 "Electricity + internet");
        rec(admin, "4500",  EXPENSE, Category.HEALTHCARE,    now.minusDays(8),                 "Insurance premium");
        rec(admin, "1200",  EXPENSE, Category.ENTERTAINMENT, now.minusDays(9),                 "OTT subscriptions");
        rec(admin, "5000",  EXPENSE, Category.EDUCATION,     now.minusDays(11),                "Online course — Spring Boot");
        rec(admin, "15000", EXPENSE, Category.SAVINGS,       now.minusDays(14),                "SIP — Mutual Fund","investment,sip");
        // Prior month
        rec(admin, "85000", INCOME,  Category.SALARY,        now.minusMonths(1).withDayOfMonth(1),  "March salary");
        rec(admin, "10000", INCOME,  Category.BONUS,         now.minusMonths(1).withDayOfMonth(15), "Performance bonus Q1");
        rec(admin, "25000", EXPENSE, Category.HOUSING,       now.minusMonths(1).withDayOfMonth(2),  "Rent — March");
        rec(admin, "7800",  EXPENSE, Category.FOOD,          now.minusMonths(1).withDayOfMonth(10), "Groceries March","food");
        rec(admin, "3000",  EXPENSE, Category.TRANSPORTATION,now.minusMonths(1).withDayOfMonth(12), "Transport March");
        rec(admin, "2200",  EXPENSE, Category.UTILITIES,     now.minusMonths(1).withDayOfMonth(5),  "Bills March");
        // 2 months ago
        rec(admin, "85000", INCOME,  Category.SALARY,        now.minusMonths(2).withDayOfMonth(1),  "February salary");
        rec(admin, "25000", EXPENSE, Category.HOUSING,       now.minusMonths(2).withDayOfMonth(2),  "Rent — February");
        rec(admin, "9100",  EXPENSE, Category.FOOD,          now.minusMonths(2).withDayOfMonth(10), "Groceries February");
        rec(admin, "6000",  EXPENSE, Category.HEALTHCARE,    now.minusMonths(2).withDayOfMonth(20), "Doctor visit + medicine");
    }

    private void seedBudgets(User admin) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end   = start.plusMonths(1).minusDays(1);

        budget(admin, Category.FOOD,           "10000", start, end, "Keep food under ₹10k");
        budget(admin, Category.ENTERTAINMENT,  "2000",  start, end, "Limit leisure spend");
        budget(admin, Category.TRANSPORTATION, "4000",  start, end, "Commute budget");
        budget(admin, Category.HEALTHCARE,     "6000",  start, end, "Medical budget");
    }

    private void seedRecurring(User admin) {
        LocalDate today = LocalDate.now();
        recurringRepo.save(RecurringTransaction.builder()
                .owner(admin).name("Monthly Salary")
                .amount(new BigDecimal("85000")).type(TransactionType.INCOME)
                .category(Category.SALARY).frequency(RecurringFrequency.MONTHLY)
                .startDate(today.withDayOfMonth(1)).description("Auto-post salary on 1st")
                .build());

        recurringRepo.save(RecurringTransaction.builder()
                .owner(admin).name("SIP Investment")
                .amount(new BigDecimal("15000")).type(TransactionType.EXPENSE)
                .category(Category.SAVINGS).frequency(RecurringFrequency.MONTHLY)
                .startDate(today.withDayOfMonth(5)).description("Mutual fund SIP")
                .build());

        recurringRepo.save(RecurringTransaction.builder()
                .owner(admin).name("Netflix + Prime")
                .amount(new BigDecimal("1200")).type(TransactionType.EXPENSE)
                .category(Category.ENTERTAINMENT).frequency(RecurringFrequency.MONTHLY)
                .startDate(today.withDayOfMonth(10)).description("Streaming subscriptions")
                .build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final TransactionType INCOME  = TransactionType.INCOME;
    private static final TransactionType EXPENSE = TransactionType.EXPENSE;

    private void rec(User u, String amount, TransactionType type, Category cat,
                     LocalDate date, String desc) {
        rec(u, amount, type, cat, date, desc, null);
    }

    private void rec(User u, String amount, TransactionType type, Category cat,
                     LocalDate date, String desc, String tags) {
        recordRepo.save(FinancialRecord.builder()
                .createdBy(u).amount(new BigDecimal(amount))
                .type(type).category(cat).date(date)
                .description(desc).tags(tags).deleted(false).build());
    }

    private void budget(User u, Category cat, String limit,
                        LocalDate start, LocalDate end, String note) {
        budgetRepo.save(Budget.builder()
                .owner(u).category(cat)
                .limitAmount(new BigDecimal(limit))
                .periodStart(start).periodEnd(end)
                .note(note).active(true).build());
    }
}
