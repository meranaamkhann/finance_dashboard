package com.finance.dashboard.config;
import com.finance.dashboard.model.*;
import com.finance.dashboard.model.enums.*;
import com.finance.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j @Configuration @Profile("dev")
@ConditionalOnProperty(name="app.seeder.enabled",havingValue="true")
@RequiredArgsConstructor
public class DataSeeder {
    private final UserRepository users;
    private final FinancialRecordRepository records;
    private final BudgetRepository budgets;
    private final RecurringTransactionRepository recurring;
    private final PasswordEncoder enc;

    @Bean CommandLineRunner seed() {
        return args -> {
            if (users.count()>0){log.info("Seeder: data exists, skipping");return;}
            log.info("Seeder: populating dev data...");
            User admin=users.save(User.builder().username("admin").email("admin@finance.dev").fullName("Admin User").password(enc.encode("Admin@1234")).role(Role.ADMIN).build());
            User analyst=users.save(User.builder().username("analyst").email("analyst@finance.dev").fullName("Analyst User").password(enc.encode("Analyst@1234")).role(Role.ANALYST).build());
            users.save(User.builder().username("viewer").email("viewer@finance.dev").fullName("Viewer User").password(enc.encode("Viewer@1234")).role(Role.VIEWER).build());

            LocalDate today=LocalDate.now(), som=today.withDayOfMonth(1), prev=som.minusMonths(1), twoAgo=som.minusMonths(2);
            LocalDate end=som.plusMonths(1).minusDays(1);

            // Current month
            rec(admin,TransactionType.INCOME, Category.SALARY,"85000",som,"Monthly salary");
            rec(admin,TransactionType.INCOME, Category.FREELANCE,"12000",today.minusDays(5),"Freelance project");
            rec(admin,TransactionType.EXPENSE,Category.RENT,"20000",som.plusDays(1),"House rent");
            rec(admin,TransactionType.EXPENSE,Category.FOOD,"4200",today.minusDays(2),"Groceries");
            rec(admin,TransactionType.EXPENSE,Category.TRANSPORT,"1800",today.minusDays(3),"Cab + metro");
            rec(admin,TransactionType.EXPENSE,Category.UTILITIES,"2100",today.minusDays(7),"Electricity + internet");
            rec(admin,TransactionType.EXPENSE,Category.ENTERTAINMENT,"1200",today.minusDays(4),"OTT + dinner");
            rec(admin,TransactionType.EXPENSE,Category.SHOPPING,"3600",today.minusDays(8),"Clothes");
            rec(admin,TransactionType.EXPENSE,Category.SUBSCRIPTION,"999",som.plusDays(2),"Streaming subscriptions");
            // Prev month
            rec(admin,TransactionType.INCOME, Category.SALARY,"85000",prev,"Prev salary");
            rec(admin,TransactionType.EXPENSE,Category.RENT,"20000",prev.plusDays(1),"Prev rent");
            rec(admin,TransactionType.EXPENSE,Category.FOOD,"5100",prev.plusDays(10),"Prev groceries");
            // 2 months ago
            rec(admin,TransactionType.INCOME, Category.SALARY,"85000",twoAgo,"2M salary");
            rec(admin,TransactionType.EXPENSE,Category.RENT,"20000",twoAgo.plusDays(1),"2M rent");
            rec(admin,TransactionType.EXPENSE,Category.FOOD,"4800",twoAgo.plusDays(8),"2M food");
            // Analyst
            rec(analyst,TransactionType.INCOME,Category.SALARY,"60000",som,"Analyst salary");
            rec(analyst,TransactionType.EXPENSE,Category.RENT,"15000",som.plusDays(1),"Analyst rent");
            rec(analyst,TransactionType.EXPENSE,Category.FOOD,"3000",today.minusDays(3),"Analyst food");

            // Budgets
            bgt(admin,Category.FOOD,"8000",som,end);
            bgt(admin,Category.ENTERTAINMENT,"3000",som,end);
            bgt(admin,Category.TRANSPORT,"3000",som,end);
            bgt(admin,Category.SHOPPING,"5000",som,end);

            // Recurring
            LocalDate next=som.plusMonths(1);
            rtx(admin,"Monthly Salary",TransactionType.INCOME,Category.SALARY,"85000",RecurringFrequency.MONTHLY,next);
            rtx(admin,"House Rent",TransactionType.EXPENSE,Category.RENT,"20000",RecurringFrequency.MONTHLY,next);
            rtx(admin,"Netflix",TransactionType.EXPENSE,Category.SUBSCRIPTION,"649",RecurringFrequency.MONTHLY,next);
            rtx(admin,"Weekly Groceries",TransactionType.EXPENSE,Category.FOOD,"1000",RecurringFrequency.WEEKLY,today.plusDays(7));

            log.info("Seeder done — admin/Admin@1234 | analyst/Analyst@1234 | viewer/Viewer@1234");
        };
    }
    private void rec(User u,TransactionType t,Category c,String a,LocalDate d,String desc){records.save(FinancialRecord.builder().type(t).category(c).amount(new BigDecimal(a)).date(d).description(desc).createdBy(u).build());}
    private void bgt(User u,Category c,String l,LocalDate s,LocalDate e){budgets.save(Budget.builder().user(u).category(c).limitAmount(new BigDecimal(l)).periodStart(s).periodEnd(e).build());}
    private void rtx(User u,String n,TransactionType t,Category c,String a,RecurringFrequency f,LocalDate nx){recurring.save(RecurringTransaction.builder().user(u).name(n).type(t).category(c).amount(new BigDecimal(a)).frequency(f).startDate(nx).nextExecutionDate(nx).build());}
}
