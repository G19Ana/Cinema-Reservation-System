package com.cinema.repository;

import com.cinema.entity.ScreeningSeat;
import com.cinema.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeat, Long> {

    List<ScreeningSeat> findByScreeningIdOrderByRowLabelAscSeatNumberAsc(Long screeningId);

    List<ScreeningSeat> findByIdInAndScreeningId(List<Long> ids, Long screeningId);

    /** All seats currently held by one browser (identified by its token). */
    List<ScreeningSeat> findByHoldTokenAndStatus(String holdToken, SeatStatus status);

    void deleteByScreeningId(Long screeningId);

    /**
     * Used by the scheduled job: frees every HELD seat whose timer expired, in ONE SQL statement.
     * A bulk UPDATE skips Hibernate's automatic version bump, so we increment version ourselves;
     * that way a user who is mid-hold on the same seat still gets an optimistic-lock conflict.
     */
    @Modifying
    @Query("""
           UPDATE ScreeningSeat s
              SET s.status = com.cinema.entity.SeatStatus.AVAILABLE,
                  s.holdToken = null, s.heldUntil = null, s.version = s.version + 1
            WHERE s.status = com.cinema.entity.SeatStatus.HELD AND s.heldUntil < :now
           """)
    int releaseExpired(@Param("now") Instant now);
}
