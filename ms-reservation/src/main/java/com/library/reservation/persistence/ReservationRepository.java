package com.library.reservation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Integer> {
    // Derived query
    List<ReservationEntity> findByUserId(Integer userId);

    // Native query: buscar por bookId
    @Query(value = "SELECT * FROM reservations r WHERE r.book_id = :bookId", nativeQuery = true)
    List<ReservationEntity> findByBookIdNative(@Param("bookId") Integer bookId);

    // Criteria-like through JPQL for date range
    @Query("SELECT r FROM ReservationEntity r WHERE r.startDate >= :start AND r.endDate <= :end")
    List<ReservationEntity> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
