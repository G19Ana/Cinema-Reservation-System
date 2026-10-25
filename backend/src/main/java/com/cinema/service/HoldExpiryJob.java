package com.cinema.service;

import com.cinema.repository.ScreeningSeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/** Background job: every 30 s, frees seats whose 10-minute hold ran out. */
@Component
public class HoldExpiryJob {
    private static final Logger log = LoggerFactory.getLogger(HoldExpiryJob.class);
    private final ScreeningSeatRepository seatRepo;

    public HoldExpiryJob(ScreeningSeatRepository seatRepo) { this.seatRepo = seatRepo; }

    @Scheduled(fixedRate = 30_000)   // milliseconds; needs @EnableScheduling on the main class
    @Transactional
    public void releaseExpiredHolds() {
        int released = seatRepo.releaseExpired(Instant.now());
        if (released > 0) log.info("Released {} expired seat holds", released);
    }
}
