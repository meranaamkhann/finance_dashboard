package com.finance.dashboard.config;

import com.finance.dashboard.repository.PlanRepository;
import com.finance.dashboard.model.Plan;
import java.math.BigDecimal;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.CustomCategory;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecurringTransaction;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.Category;
import com.finance.dashboard.model.enums.RecurringFrequency;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.repository.BudgetRepository;
import com.finance.dashboard.repository.CustomCategoryRepository;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.RecurringTransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Slf4j
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepo;
    private final FinancialRecordRepository recordRepo;
    private final BudgetRepository budgetRepo;
    private final RecurringTransactionRepository recurringRepo;
    private final PasswordEncoder encoder;
    private final PlanRepository planRepo;
    private final CustomCategoryRepository customCategoryRepo;

    @Bean
    CommandLineRunner seed() {

        if (planRepo.count() == 0) {

            planRepo.save(Plan.builder()
                    .name("Free")
                    .slug("free")
                    .description("Get started at no cost")
                    .monthlyPrice(BigDecimal.ZERO)
                    .yearlyPrice(BigDecimal.ZERO)
                    .maxRecords(100)
                    .maxBudgets(3)
                    .maxRecurring(3)
                    .maxExports(5)
                    .maxUsers(1)
                    .apiAccess(false)
                    .prioritySupport(false)
                    .sortOrder(0)
                    .visible(true)
                    .active(true)
                    .features(java.util.List.of(
                            "100 records/month",
                            "3 budgets",
                            "Basic dashboard",
                            "CSV export"
                    ))
                    .build());

            planRepo.save(Plan.builder()
                    .name("Pro")
                    .slug("pro")
                    .description("For serious finance tracking")
                    .monthlyPrice(new BigDecimal("299"))
                    .yearlyPrice(new BigDecimal("2990"))
                    .maxRecords(Integer.MAX_VALUE)
                    .maxBudgets(Integer.MAX_VALUE)
                    .maxRecurring(Integer.MAX_VALUE)
                    .maxExports(Integer.MAX_VALUE)
                    .maxUsers(1)
                    .apiAccess(false)
                    .prioritySupport(true)
                    .sortOrder(1)
                    .visible(true)
                    .active(true)
                    .features(java.util.List.of(
                            "Unlimited records",
                            "Unlimited budgets",
                            "Full analytics",
                            "CSV export",
                            "Recurring automation",
                            "Priority support"
                    ))
                    .build());

            planRepo.save(Plan.builder()
                    .name("Team")
                    .slug("team")
                    .description("For teams and businesses")
                    .monthlyPrice(new BigDecimal("799"))
                    .yearlyPrice(new BigDecimal("7990"))
                    .maxRecords(Integer.MAX_VALUE)
                    .maxBudgets(Integer.MAX_VALUE)
                    .maxRecurring(Integer.MAX_VALUE)
                    .maxExports(Integer.MAX_VALUE)
                    .maxUsers(5)
                    .apiAccess(true)
                    .prioritySupport(true)
                    .sortOrder(2)
                    .visible(true)
                    .active(true)
                    .features(java.util.List.of(
                            "Everything in Pro",
                            "5 users",
                            "RBAC",
                            "Audit trail",
                            "API access"
                    ))
                    .build());

            log.info("Plans seeded: Free, Pro, Team");
        }

        return args -> {

            // Always make sure system categories exist
            // and have the correct colors.
            seedSystemCategories();

            // Existing dev data should not be recreated.
            if (userRepo.count() > 0) {
                log.info("DataSeeder: data exists, skipping user/record data");
                return;
            }

            log.info("DataSeeder: seeding dev data...");

            User admin = save(user(
                    "admin",
                    "admin@finance.dev",
                    "Admin User",
                    "Admin@1234",
                    Role.ADMIN
            ));

            User analyst = save(user(
                    "analyst",
                    "analyst@finance.dev",
                    "Analyst User",
                    "Analyst@1234",
                    Role.ANALYST
            ));

            save(user(
                    "viewer",
                    "viewer@finance.dev",
                    "Viewer User",
                    "Viewer@1234",
                    Role.VIEWER
            ));

            LocalDate today = LocalDate.now();
            LocalDate som = today.withDayOfMonth(1);
            LocalDate prev = som.minusMonths(1);
            LocalDate ago2 = som.minusMonths(2);
            LocalDate end = som.plusMonths(1).minusDays(1);

            rec(admin, TransactionType.INCOME, Category.SALARY,
                    "85000", som, "Monthly salary");

            rec(admin, TransactionType.INCOME, Category.FREELANCE,
                    "12000", today.minusDays(5), "Freelance project");

            rec(admin, TransactionType.EXPENSE, Category.RENT,
                    "20000", som.plusDays(1), "House rent");

            rec(admin, TransactionType.EXPENSE, Category.FOOD,
                    "4200", today.minusDays(2), "Groceries");

            rec(admin, TransactionType.EXPENSE, Category.TRANSPORT,
                    "1800", today.minusDays(3), "Cab + metro");

            rec(admin, TransactionType.EXPENSE, Category.UTILITIES,
                    "2100", today.minusDays(7), "Electricity + internet");

            rec(admin, TransactionType.EXPENSE, Category.ENTERTAINMENT,
                    "1200", today.minusDays(4), "OTT + dinner");

            rec(admin, TransactionType.EXPENSE, Category.SHOPPING,
                    "3600", today.minusDays(8), "Clothes");

            rec(admin, TransactionType.EXPENSE, Category.SUBSCRIPTION,
                    "999", som.plusDays(2), "Streaming subs");

            rec(admin, TransactionType.INCOME, Category.SALARY,
                    "85000", prev, "Prev salary");

            rec(admin, TransactionType.EXPENSE, Category.RENT,
                    "20000", prev.plusDays(1), "Prev rent");

            rec(admin, TransactionType.EXPENSE, Category.FOOD,
                    "5100", prev.plusDays(10), "Prev groceries");

            rec(admin, TransactionType.INCOME, Category.SALARY,
                    "85000", ago2, "2M salary");

            rec(admin, TransactionType.EXPENSE, Category.RENT,
                    "20000", ago2.plusDays(1), "2M rent");

            rec(admin, TransactionType.EXPENSE, Category.FOOD,
                    "4800", ago2.plusDays(8), "2M food");

            rec(analyst, TransactionType.INCOME, Category.SALARY,
                    "60000", som, "Analyst salary");

            rec(analyst, TransactionType.EXPENSE, Category.RENT,
                    "15000", som.plusDays(1), "Analyst rent");

            rec(analyst, TransactionType.EXPENSE, Category.FOOD,
                    "3000", today.minusDays(3), "Analyst food");

            bgt(admin, Category.FOOD, "8000", som, end);
            bgt(admin, Category.ENTERTAINMENT, "3000", som, end);
            bgt(admin, Category.TRANSPORT, "3000", som, end);
            bgt(admin, Category.SHOPPING, "5000", som, end);

            LocalDate next = som.plusMonths(1);

            rtx(
                    admin,
                    "Monthly Salary",
                    TransactionType.INCOME,
                    Category.SALARY,
                    "85000",
                    RecurringFrequency.MONTHLY,
                    next
            );

            rtx(
                    admin,
                    "House Rent",
                    TransactionType.EXPENSE,
                    Category.RENT,
                    "20000",
                    RecurringFrequency.MONTHLY,
                    next
            );

            rtx(
                    admin,
                    "Netflix",
                    TransactionType.EXPENSE,
                    Category.SUBSCRIPTION,
                    "649",
                    RecurringFrequency.MONTHLY,
                    next
            );

            rtx(
                    admin,
                    "Weekly Groceries",
                    TransactionType.EXPENSE,
                    Category.FOOD,
                    "1000",
                    RecurringFrequency.WEEKLY,
                    today.plusDays(7)
            );

            log.info(
                    "DataSeeder done — admin/Admin@1234 | analyst/Analyst@1234 | viewer/Viewer@1234"
            );
        };
    }

    private User user(
            String username,
            String email,
            String fullName,
            String pwd,
            Role role
    ) {
        return User.builder()
                .username(username)
                .email(email)
                .fullName(fullName)
                .password(encoder.encode(pwd))
                .role(role)
                .build();
    }

    private User save(User u) {
        return userRepo.save(u);
    }

    private void rec(
            User u,
            TransactionType t,
            Category c,
            String amt,
            LocalDate d,
            String desc
    ) {
        recordRepo.save(
                FinancialRecord.builder()
                        .type(t)
                        .category(c)
                        .amount(new BigDecimal(amt))
                        .date(d)
                        .description(desc)
                        .createdBy(u)
                        .build()
        );
    }

    private void bgt(
            User u,
            Category c,
            String lim,
            LocalDate s,
            LocalDate e
    ) {
        budgetRepo.save(
                Budget.builder()
                        .user(u)
                        .category(c)
                        .limitAmount(new BigDecimal(lim))
                        .periodStart(s)
                        .periodEnd(e)
                        .build()
        );
    }

    private void rtx(
            User u,
            String name,
            TransactionType t,
            Category c,
            String amt,
            RecurringFrequency f,
            LocalDate next
    ) {
        recurringRepo.save(
                RecurringTransaction.builder()
                        .user(u)
                        .name(name)
                        .type(t)
                        .category(c)
                        .amount(new BigDecimal(amt))
                        .frequency(f)
                        .startDate(next)
                        .nextExecutionDate(next)
                        .build()
        );
    }

    private void seedSystemCategories() {

        seedCategory("SALARY", "#22c55e", "INCOME");
        seedCategory("FREELANCE", "#14b8a6", "INCOME");

        seedCategory("FOOD", "#f97316", "EXPENSE");
        seedCategory("RENT", "#3b82f6", "EXPENSE");
        seedCategory("TRANSPORT", "#8b5cf6", "EXPENSE");
        seedCategory("UTILITIES", "#06b6d4", "EXPENSE");
        seedCategory("ENTERTAINMENT", "#ec4899", "EXPENSE");
        seedCategory("SHOPPING", "#f59e0b", "EXPENSE");
        seedCategory("SUBSCRIPTION", "#a855f7", "EXPENSE");
        seedCategory("HEALTHCARE", "#ef4444", "EXPENSE");
        seedCategory("EDUCATION", "#10b981", "EXPENSE");
        seedCategory("TRAVEL", "#14b8a6", "EXPENSE");
        seedCategory("INSURANCE", "#6366f1", "EXPENSE");
        seedCategory("EMI", "#64748b", "EXPENSE");
        seedCategory("PERSONAL_CARE", "#e11d48", "EXPENSE");
        seedCategory("HOME", "#84cc16", "EXPENSE");
        seedCategory("OTHER_EXPENSE", "#78716c", "EXPENSE");

        log.info("System categories seeded/updated");
    }

    private void seedCategory(
            String name,
            String color,
            String type
    ) {

        customCategoryRepo
                .findByNameIgnoreCaseAndSystemTrue(name)
                .ifPresentOrElse(

                        existing -> {
                            existing.setColor(color);
                            existing.setType(type);
                            existing.setSystem(true);
                            customCategoryRepo.save(existing);
                        },

                        () -> customCategoryRepo.save(
                                CustomCategory.builder()
                                        .name(name)
                                        .color(color)
                                        .type(type)
                                        .system(true)
                                        .workspaceId(null)
                                        .createdBy(null)
                                        .build()
                        )
                );
    }
}