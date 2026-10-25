package com.cinema.controller;

import com.cinema.dto.Dtos.*;
import com.cinema.service.ScreeningService;
import com.cinema.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** /api/screenings - browse screenings, see the seat map, hold seats, (admin) create/delete. */
@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {
    private final ScreeningService screeningService;
    private final SeatService seatService;

    public ScreeningController(ScreeningService screeningService, SeatService seatService) {
        this.screeningService = screeningService; this.seatService = seatService;
    }

    @GetMapping
    public List<ScreeningDto> list(@RequestParam(required = false) Long movieId) { return screeningService.list(movieId); }

    @GetMapping("/{id}")
    public ScreeningDto get(@PathVariable Long id) { return screeningService.get(id); }

    /** Polled by the Angular seat map every few seconds -> "live" availability. */
    @GetMapping("/{id}/seats")
    public List<SeatDto> seats(@PathVariable Long id, @RequestParam(defaultValue = "") String token) {
        return screeningService.seatMap(id, token);
    }

    @PostMapping("/{id}/hold")
    public HoldResponse hold(@PathVariable Long id, @Valid @RequestBody HoldRequest req) {
        return new HoldResponse(seatService.holdSeats(id, req.seatIds(), req.token()));
    }

    @PostMapping
    public ScreeningDto create(@Valid @RequestBody ScreeningRequest req) { return screeningService.create(req); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { screeningService.delete(id); }
}
