package com.cinema.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs (Data Transfer Objects) = the exact JSON shapes the API sends/receives.
 * Using them instead of entities keeps database internals (version, lazy relations) out of the API.
 * Java "records" are immutable classes with auto-generated getters, perfect for this.
 */
public class Dtos {

    // ----- responses -----
    public record ScreeningDto(Long id, Long movieId, String movieTitle, String hallName,
                               LocalDateTime startTime, BigDecimal price, int rows, int seatsPerRow) {}

    /** status is "AVAILABLE", "HELD" or "BOOKED"; mine=true when the asking browser holds it. */
    public record SeatDto(Long id, String row, int number, String status, boolean mine) {}

    public record HoldResponse(Instant holdUntil) {}

    public record BookingDto(String code, String movieTitle, String hallName, LocalDateTime startTime,
                             List<String> seats, BigDecimal total, String customerName) {}

    // ----- requests (with validation rules) -----
    public record HoldRequest(@NotEmpty List<Long> seatIds, @NotBlank String token) {}

    public record CheckoutRequest(@NotBlank String token, @NotBlank String name,
                                  @Email @NotBlank String email,
                                  @Size(min = 12, message = "Card number too short") String cardNumber) {}

    public record ScreeningRequest(@NotNull Long movieId, @NotBlank String hallName,
                                   @NotNull LocalDateTime startTime, @NotNull @Positive BigDecimal price,
                                   @Min(1) @Max(26) int rows, @Min(1) @Max(40) int seatsPerRow) {}
}
