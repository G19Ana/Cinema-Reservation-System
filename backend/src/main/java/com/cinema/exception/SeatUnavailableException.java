package com.cinema.exception;

/** Thrown when a seat is already taken or a hold has expired. Mapped to HTTP 409 Conflict. */
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) { super(message); }
}
