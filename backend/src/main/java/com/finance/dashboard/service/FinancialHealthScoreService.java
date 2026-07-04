package com.finance.dashboard.service;
import com.finance.dashboard.dto.response.FinancialHealthScoreResponse;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor
public class FinancialHealthScoreService {
    private final FinancialRecordRepository recordRepository;

    @Transactional(readOnly=true)
    public FinancialHealthScoreResponse calculate(Long userId) {
        LocalDate from = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        Map<String,BigDecimal> incomeMap  = toMonthMap(recordRepository.monthlyAmountByTypeAndUser(userId, TransactionType.INCOME,  from));
        Map<String,BigDecimal> expenseMap = toMonthMap(recordRepository.monthlyAmountByTypeAndUser(userId, TransactionType.EXPENSE, from));
        Set<String> allMonths = new LinkedHashSet<>(incomeMap.keySet()); allMonths.addAll(expenseMap.keySet());

        if (allMonths.isEmpty())
            return FinancialHealthScoreResponse.builder().score(0).grade("N/A").breakdown(Map.of())
                    .insights(List.of("No data in last 6 months. Record income and expenses to get your score.")).build();

        List<Double> incomes=new ArrayList<>(), expenses=new ArrayList<>(), nets=new ArrayList<>();
        for (String m : allMonths) {
            double inc=incomeMap.getOrDefault(m,BigDecimal.ZERO).doubleValue();
            double exp=expenseMap.getOrDefault(m,BigDecimal.ZERO).doubleValue();
            incomes.add(inc); expenses.add(exp); nets.add(inc-exp);
        }
        double totalIncome=incomes.stream().mapToDouble(d->d).sum(), totalExpense=expenses.stream().mapToDouble(d->d).sum();
        double savingsRate=totalIncome>0?(totalIncome-totalExpense)/totalIncome*100:0;
        double savingsPts=clamp(savingsRate/20.0*30,0,30);
        double expCv=cv(expenses), expPts=clamp((1-Math.min(expCv,1))*20,0,20);
        double incCv=cv(incomes),  incPts=clamp((1-Math.min(incCv,1))*20,0,20);
        long posMonths=nets.stream().filter(n->n>=0).count();
        double netPts=(double)posMonths/nets.size()*20;
        double eiRatio=totalIncome>0?totalExpense/totalIncome:1.0, eiPts=clamp((1-Math.min(eiRatio,1))*10,0,10);
        int score=Math.max(0,Math.min(100,(int)Math.round(savingsPts+expPts+incPts+netPts+eiPts)));

        Map<String,Double> breakdown=new LinkedHashMap<>();
        breakdown.put("Savings Rate (max 30)",r2(savingsPts));
        breakdown.put("Expense Consistency (max 20)",r2(expPts));
        breakdown.put("Income Consistency (max 20)",r2(incPts));
        breakdown.put("Positive Net Months (max 20)",r2(netPts));
        breakdown.put("Expense-to-Income (max 10)",r2(eiPts));

        return FinancialHealthScoreResponse.builder().score(score).grade(grade(score))
                .breakdown(breakdown).insights(insights(savingsRate,expCv,eiRatio,posMonths,nets.size())).build();
    }

    private Map<String,BigDecimal> toMonthMap(List<Object[]> rows) {
        Map<String,BigDecimal> m=new LinkedHashMap<>();
        for(Object[] r:rows) m.put(r[0]+"-"+r[1], new BigDecimal(r[2].toString())); return m;
    }
    private double cv(List<Double> vals) {
        if(vals.size()<2) return 0;
        double mean=vals.stream().mapToDouble(d->d).average().orElse(0); if(mean==0) return 0;
        return Math.sqrt(vals.stream().mapToDouble(v->Math.pow(v-mean,2)).average().orElse(0))/mean;
    }
    private double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
    private double r2(double v){return BigDecimal.valueOf(v).setScale(2,RoundingMode.HALF_UP).doubleValue();}
    private String grade(int s){return s>=85?"A":s>=70?"B":s>=55?"C":s>=40?"D":"F";}
    private List<String> insights(double sr,double expCv,double eiRatio,long pos,int total){
        List<String> i=new ArrayList<>();
        if(sr<0)         i.add("🚨 Spending exceeds income. Immediate action required.");
        else if(sr<10)   i.add("💡 Savings rate below 10%. Target 20%+ by reducing discretionary spend.");
        else if(sr<20)   i.add("📈 Good start! Push savings rate above 20% for stronger security.");
        else             i.add(String.format("✅ Excellent savings rate of %.1f%%. Keep it up!",sr));
        if(expCv>0.5)    i.add("⚠️ Expenses vary a lot month-to-month. Set category budgets to stabilise.");
        if(eiRatio>0.9)  i.add("🚨 Spending over 90% of income. Cut non-essential expenses now.");
        else if(eiRatio>0.75) i.add("⚠️ High expense ratio. Review subscriptions and dining.");
        if(pos<total)    i.add(String.format("📉 %d month(s) with negative net balance. Build an emergency fund.",(total-pos)));
        if(i.stream().noneMatch(s->s.startsWith("🚨")||s.startsWith("⚠️")||s.startsWith("📉")))
            i.add("🏆 Outstanding financial health! Keep up these habits.");
        return i;
    }
}
