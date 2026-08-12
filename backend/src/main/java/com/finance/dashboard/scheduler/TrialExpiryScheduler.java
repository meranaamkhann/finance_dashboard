package com.finance.dashboard.scheduler;

import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrialExpiryScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireTrials() {
        userRepository.findExpiredTrials(LocalDateTime.now()).forEach(user -> {
            user.setRole(Role.VIEWER);
            user.setOnTrial(false);
            userRepository.save(user);
            log.info("Trial expired for user: {} — downgraded to VIEWER", user.getUsername());
        });
    }
}