package com.finance.dashboard.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KeepAliveScheduler {

    @Scheduled(fixedDelay = 840000)
    public void keepAlive() {
        log.debug("Keep-alive ping");
    }
}