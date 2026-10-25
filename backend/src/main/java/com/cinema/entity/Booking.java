package com.cinema.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A confirmed purchase: who bought which seats for which screening. */
@Entity
@Getter @Setter
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;            // shown on the ticket, e.g. "K7Q2MZ4A"

    private String customerName;
    private String email;
    private BigDecimal totalPrice;
    private Instant createdAt = Instant.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Screening screening;

    @OneToMany(mappedBy = "booking")
    private List<ScreeningSeat> seats = new ArrayList<>();
}
