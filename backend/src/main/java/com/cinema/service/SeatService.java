package com.cinema.service;

import com.cinema.entity.ScreeningSeat;
import com.cinema.entity.SeatStatus;
import com.cinema.exception.SeatUnavailableException;
import com.cinema.repository.ScreeningSeatRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Holding seats for a limited time while the user fills in the checkout form. */
@Service
public class SeatService {
    private final ScreeningSeatRepository seatRepo;
    private final long holdMinutes;

    public SeatService(ScreeningSeatRepository seatRepo, @Value("${cinema.hold-minutes}") long holdMinutes) {
        this.seatRepo = seatRepo;
        this.holdMinutes = holdMinutes;
    }

    /**
     * Holds the requested seats for this browser (token) for N minutes.
     * All-or-nothing: if ANY seat is taken the whole transaction rolls back.
     *
     * Where the optimistic lock bites: two requests for the same seat both read it as AVAILABLE
     * (version 0). Both call seat.hold(). On flush the first UPDATE ... WHERE version=0 wins; the
     * second matches zero rows and Hibernate throws ObjectOptimisticLockingFailureException (-> 409).
     */
    @Transactional
    public Instant holdSeats(Long screeningId, List<Long> seatIds, String token) {
        Instant now = Instant.now();

        // 1) Drop this browser's previous selection, so changing your mind frees the old seats.
        seatRepo.findByHoldTokenAndStatus(token, SeatStatus.HELD).forEach(ScreeningSeat::release);

        // 2) Load the requested seats and make sure they all belong to this screening.
        List<ScreeningSeat> seats = seatRepo.findByIdInAndScreeningId(seatIds, screeningId);
        if (seats.size() != seatIds.size()) throw new SeatUnavailableException("Unknown seat selected.");

        // 3) Check availability, then hold.
        Instant until = now.plus(Duration.ofMinutes(holdMinutes));
        for (ScreeningSeat s : seats) {
            if (!s.isFree(now) && !token.equals(s.getHoldToken()))
                throw new SeatUnavailableException("Seat " + s.label() + " is no longer available.");
            s.hold(token, until);
        }

        // 4) flush() sends the UPDATEs now, so a version conflict is raised inside this method.
        seatRepo.flush();
        return until;
    }
}
