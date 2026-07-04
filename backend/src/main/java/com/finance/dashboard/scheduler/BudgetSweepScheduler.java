package com.finance.dashboard.scheduler;
import com.finance.dashboard.service.BudgetAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j @Component @RequiredArgsConstructor
public class BudgetSweepScheduler {
    private final BudgetAlertService budgetAlertService;
    @Scheduled(cron="${app.scheduler.budget-sweep-cron:0 0 2 * * *}")
    public void sweep() { log.info("Budget sweep triggered"); try{budgetAlertService.sweepAll();}catch(Exception e){log.error("Sweep failed: {}",e.getMessage());} }
}
