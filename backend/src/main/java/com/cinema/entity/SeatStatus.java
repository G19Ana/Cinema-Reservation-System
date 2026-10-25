package com.cinema.entity;

/** AVAILABLE -> HELD (10 min timer) -> BOOKED. HELD falls back to AVAILABLE on timeout. */
public enum SeatStatus { AVAILABLE, HELD, BOOKED }
