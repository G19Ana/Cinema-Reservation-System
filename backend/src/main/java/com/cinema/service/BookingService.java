package com.cinema.service;

import com.cinema.dto.Dtos.*;
import com.cinema.entity.*;
import com.cinema.exception.SeatUnavailableException;
import com.cinema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/** Turns a seat hold into a confirmed booking (checkout). */
@Service
public class BookingService {
    private final ScreeningSeatRepository seatRepo;
    private final BookingRepository bookingRepo;

    public BookingService(ScreeningSeatRepository seatRepo, BookingRepository bookingRepo) {
        this.seatRepo = seatRepo; this.bookingRepo = bookingRepo;
    }

    @Transactional
    public BookingDto checkout(CheckoutRequest req) {
        Instant now = Instant.now();

        // The seats this browser is holding. If the timer ran out, they are no longer ours.
        List<ScreeningSeat> seats = seatRepo.findByHoldTokenAndStatus(req.token(), SeatStatus.HELD);
        if (seats.isEmpty() || seats.stream().anyMatch(s -> s.getHeldUntil().isBefore(now)))
            throw new SeatUnavailableException("Your 10-minute hold expired. Please select your seats again.");

        // Simulated payment: a real app would call Stripe/PayPal here. The card number is NEVER stored.
        // (To test a failed payment, use a card number starting with 0000.)
        if (req.cardNumber().startsWith("0000"))
            throw new SeatUnavailableException("Payment declined. Try another card.");

        Screening screening = seats.get(0).getScreening();
        Booking b = new Booking();
        b.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        b.setCustomerName(req.name());
        b.setEmail(req.email());
        b.setScreening(screening);
        b.setTotalPrice(screening.getPrice().multiply(BigDecimal.valueOf(seats.size())));
        bookingRepo.save(b);

        // Mark seats as sold. Version check applies here too (e.g. if the expiry job touched them).
        seats.forEach(s -> s.book(b));
        seatRepo.flush();
        return toDto(b, seats);
    }

    @Transactional(readOnly = true)
    public BookingDto getByCode(String code) {
        Booking b = bookingRepo.findByCode(code).orElseThrow(() -> new NoSuchElementException("Booking not found"));
        return toDto(b, b.getSeats());
    }

    private BookingDto toDto(Booking b, List<ScreeningSeat> seats) {
        Screening s = b.getScreening();
        List<String> labels = seats.stream().map(ScreeningSeat::label).sorted().toList();
        return new BookingDto(b.getCode(), s.getMovie().getTitle(), s.getHallName(), s.getStartTime(),
                labels, b.getTotalPrice(), b.getCustomerName());
    }
}
