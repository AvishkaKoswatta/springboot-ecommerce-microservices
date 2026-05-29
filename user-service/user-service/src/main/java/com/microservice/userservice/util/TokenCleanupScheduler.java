package com.microservice.userservice.util;

import com.microservice.userservice.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final VerificationTokenRepository tokenRepository;

    /**
     * Runs every day at 2:00 AM to delete expired, used tokens.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanUpExpiredTokens() {
        log.info("Running expired token cleanup at {}", LocalDateTime.now());
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired token cleanup complete.");
    }
}
