package com.library.repository;

import com.library.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByBookIdAndStatusOrderByReservedAtAsc(Long bookId, String status);
    List<Reservation> findByUserId(Long userId);
    Optional<Reservation> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
}