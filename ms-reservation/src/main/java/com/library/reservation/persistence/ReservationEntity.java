package com.library.reservation.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
public class ReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservationId;

    @Column(nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
