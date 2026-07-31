package com.finance.dashboard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class FinanceDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceDashboardApplication.class, args);
    }
}