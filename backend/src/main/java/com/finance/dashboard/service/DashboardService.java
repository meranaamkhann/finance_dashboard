package com.finance.dashboard.service;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.model.enums.*;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @RequiredArgsConstructor
public class DashboardService {
    private final FinancialRecordRepository recordRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringTransactionRepository recurringRepository;
    private final NotificationRepository notificationRepository;
    private final FinancialHealthScoreService healthScoreService;
    private final SecurityUtils securityUtils;
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");

    @Transactional(readOnly=true)
    public DashboardSummaryResponse getSummary() {
        Long uid=securityUtils.getCurrentUserId();
        LocalDate som=LocalDate.now().withDayOfMonth(1), today=LocalDate.now();
        BigDecimal income =zero(recordRepository.sumByUserAndTypeAndDateBetween(uid,TransactionType.INCOME, som,today));
        BigDecimal expense=zero(recordRepository.sumByUserAndTypeAndDateBetween(uid,TransactionType.EXPENSE,som,today));
        BigDecimal net=income.subtract(expense);
        BigDecimal sr=income.compareTo(BigDecimal.ZERO)>0
                ?net.divide(income,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.HALF_UP)
                :BigDecimal.ZERO;
        return DashboardSummaryResponse.builder().totalIncome(income).totalExpense(expense).netBalance(net).savingsRate(sr)
                .totalRecords(recordRepository.countByCreatedByIdAndDeletedFalse(uid))
                .activeBudgets(budgetRepository.findAllByUserIdAndActiveTrue(uid).size())
                .activeRecurring(recurringRepository.findAllByUserIdAndActiveTrue(uid).size())
                .unreadNotifications(notificationRepository.countByUserIdAndReadFalse(uid))
                .healthScore(healthScoreService.calculate(uid))
                .topCategories(getCategoryBreakdown(uid,som,today).stream().limit(5).toList()).build();
    }

    @Transactional(readOnly=true)
    public DashboardSummaryResponse getSummaryForRange(LocalDate from, LocalDate to) {
        validateRange(from,to); Long uid=securityUtils.getCurrentUserId();
        BigDecimal income =zero(recordRepository.sumByUserAndTypeAndDateBetween(uid,TransactionType.INCOME, from,to));
        BigDecimal expense=zero(recordRepository.sumByUserAndTypeAndDateBetween(uid,TransactionType.EXPENSE,from,to));
        BigDecimal net=income.subtract(expense);
        BigDecimal sr=income.compareTo(BigDecimal.ZERO)>0
                ?net.divide(income,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.HALF_UP)
                :BigDecimal.ZERO;
        return DashboardSummaryResponse.builder().totalIncome(income).totalExpense(expense).netBalance(net).savingsRate(sr)
                .topCategories(getCategoryBreakdown(uid,from,to)).build();
    }

    @Transactional(readOnly=true)
    public List<CategorySummaryResponse> getCategoryBreakdown(LocalDate from, LocalDate to) {
        return getCategoryBreakdown(securityUtils.getCurrentUserId(),from,to);
    }

    private List<CategorySummaryResponse> getCategoryBreakdown(Long uid, LocalDate from, LocalDate to) {
        List<Object[]> rows=recordRepository.categoryBreakdownByUser(uid,from,to);
        BigDecimal total=rows.stream().map(r->new BigDecimal(r[1].toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
        return rows.stream().map(r->{
            BigDecimal amt=new BigDecimal(r[1].toString());
            double pct=total.compareTo(BigDecimal.ZERO)>0?amt.divide(total,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue():0;
            return CategorySummaryResponse.builder().category(Category.valueOf(r[0].toString())).amount(amt).percentage(Math.round(pct*100.0)/100.0).build();
        }).toList();
    }

    @Transactional(readOnly=true)
    public List<MonthlyTrendResponse> getMonthlyTrend(int months) {
        Long uid=securityUtils.getCurrentUserId();
        LocalDate from=LocalDate.now().minusMonths(months).withDayOfMonth(1),to=LocalDate.now();
        return recordRepository.monthlyTrendByUser(uid,from,to).stream().map(r->{
            int year=((Number)r[0]).intValue(),month=((Number)r[1]).intValue();
            BigDecimal inc=r[2]!=null?new BigDecimal(r[2].toString()):BigDecimal.ZERO;
            BigDecimal exp=r[3]!=null?new BigDecimal(r[3].toString()):BigDecimal.ZERO;
            return MonthlyTrendResponse.builder().year(year).month(month)
                    .monthLabel(YearMonth.of(year,month).format(MONTH_FMT))
                    .income(inc).expense(exp).net(inc.subtract(exp)).build();
        }).toList();
    }

    @Transactional(readOnly=true)
    public List<CategorySummaryResponse> getTopExpenses(LocalDate from, LocalDate to, int limit) {
        validateRange(from,to);
        return getCategoryBreakdown(securityUtils.getCurrentUserId(),from,to).stream().limit(limit).toList();
    }

    @Transactional(readOnly=true)
    public Map<String,BigDecimal> getSpendingByDayOfWeek(LocalDate from, LocalDate to) {
        validateRange(from,to); Long uid=securityUtils.getCurrentUserId();
        String[] days={"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        Map<String,BigDecimal> result=new LinkedHashMap<>();
        recordRepository.spendingByDayOfWeekByUser(uid,from,to).forEach(r->{
            int dow=((Number)r[0]).intValue();
            result.put(dow>=0&&dow<7?days[dow]:"Unknown",new BigDecimal(r[1].toString()));
        });
        return result;
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if(from!=null&&to!=null&&from.isAfter(to)) throw new BadRequestException("'from' must be before 'to'");
    }
    private BigDecimal zero(BigDecimal v){return v!=null?v:BigDecimal.ZERO;}
}
