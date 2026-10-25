package com.cinema.repository;

import com.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByCode(String code);
    void deleteByScreeningId(Long screeningId);
}
