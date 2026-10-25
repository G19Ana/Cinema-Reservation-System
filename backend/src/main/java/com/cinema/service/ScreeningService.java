package com.cinema.service;

import com.cinema.dto.Dtos.*;
import com.cinema.entity.*;
import com.cinema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

/** Admin operations on screenings + building the seat map for the UI. */
@Service
public class ScreeningService {
    private final ScreeningRepository screeningRepo;
    private final MovieRepository movieRepo;
    private final ScreeningSeatRepository seatRepo;
    private final BookingRepository bookingRepo;

    public ScreeningService(ScreeningRepository s, MovieRepository m, ScreeningSeatRepository seats, BookingRepository b) {
        this.screeningRepo = s; this.movieRepo = m; this.seatRepo = seats; this.bookingRepo = b;
    }

    /** Creates the screening AND one ScreeningSeat per seat (A1..A10, B1..B10, ...). */
    @Transactional
    public ScreeningDto create(ScreeningRequest r) {
        Movie movie = movieRepo.findById(r.movieId()).orElseThrow(() -> new NoSuchElementException("Movie not found"));
        Screening s = new Screening();
        s.setMovie(movie); s.setHallName(r.hallName()); s.setStartTime(r.startTime());
        s.setPrice(r.price()); s.setSeatRows(r.rows()); s.setSeatsPerRow(r.seatsPerRow());
        screeningRepo.save(s);

        for (int row = 0; row < r.rows(); row++) {
            for (int n = 1; n <= r.seatsPerRow(); n++) {
                ScreeningSeat seat = new ScreeningSeat();
                seat.setScreening(s);
                seat.setRowLabel(String.valueOf((char) ('A' + row)));
                seat.setSeatNumber(n);
                seatRepo.save(seat);
            }
        }
        return toDto(s);
    }

    @Transactional(readOnly = true)
    public List<ScreeningDto> list(Long movieId) {
        List<Screening> list = movieId == null ? screeningRepo.findAllByOrderByStartTime()
                                               : screeningRepo.findByMovieIdOrderByStartTime(movieId);
        return list.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ScreeningDto get(Long id) {
        return toDto(screeningRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Screening not found")));
    }

    /** Seat map for the UI. Expired holds are shown as AVAILABLE even before the job cleans them. */
    @Transactional(readOnly = true)
    public List<SeatDto> seatMap(Long screeningId, String token) {
        Instant now = Instant.now();
        return seatRepo.findByScreeningIdOrderByRowLabelAscSeatNumberAsc(screeningId).stream().map(s -> {
            boolean mine = s.getStatus() == SeatStatus.HELD && !s.isFree(now) && s.getHoldToken().equals(token);
            String status = s.isFree(now) ? "AVAILABLE" : s.getStatus().name();
            return new SeatDto(s.getId(), s.getRowLabel(), s.getSeatNumber(), status, mine);
        }).toList();
    }

    /** Delete order matters because of foreign keys: seats -> bookings -> screening. */
    @Transactional
    public void delete(Long id) {
        seatRepo.deleteByScreeningId(id);
        bookingRepo.deleteByScreeningId(id);
        screeningRepo.deleteById(id);
    }

    @Transactional
    public void deleteMovie(Long movieId) {
        screeningRepo.findByMovieIdOrderByStartTime(movieId).forEach(s -> delete(s.getId()));
        movieRepo.deleteById(movieId);
    }

    private ScreeningDto toDto(Screening s) {
        return new ScreeningDto(s.getId(), s.getMovie().getId(), s.getMovie().getTitle(), s.getHallName(),
                s.getStartTime(), s.getPrice(), s.getSeatRows(), s.getSeatsPerRow());
    }
}
