package com.cinema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One showing of a movie in a hall at a given time.
 * The hall layout (seatRows x seatsPerRow) is stored here, and one ScreeningSeat
 * row is generated per seat when the screening is created (ScreeningService).
 */
@Entity
@Getter @Setter
public class Screening {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Movie movie;

    private String hallName;
    private LocalDateTime startTime;
    private BigDecimal price;     // price per seat
    private int seatRows;         // "rows" is a reserved word in some SQL dialects, so seatRows
    private int seatsPerRow;
}
