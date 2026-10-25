package com.cinema.repository;

import com.cinema.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    // Method names are parsed by Spring Data: "find by movie.id, order by startTime"
    List<Screening> findByMovieIdOrderByStartTime(Long movieId);
    List<Screening> findAllByOrderByStartTime();
}
