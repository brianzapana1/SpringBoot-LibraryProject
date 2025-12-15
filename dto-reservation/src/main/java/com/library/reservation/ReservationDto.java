package com.library.reservation;

public class ReservationDto {
    private Integer reservationId;
    private Integer bookId;
    private Integer userId;
    private String startDate;
    private String endDate;

    public ReservationDto() {}

    public ReservationDto(Integer reservationId, Integer bookId, Integer userId, String startDate, String endDate) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
