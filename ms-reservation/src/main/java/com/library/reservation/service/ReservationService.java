package com.library.reservation.service;

import com.library.reservation.ReservationDto;
import com.library.reservation.persistence.ReservationEntity;
import com.library.reservation.persistence.ReservationRepository;
import bo.edu.ucb.microservices.util.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }

    public ReservationDto getById(Integer id) {
        ReservationEntity e = repository.findById(id).orElseThrow(() -> new NotFoundException("No se encontró reserva con id: " + id));
        return toDto(e);
    }

    public ReservationDto create(ReservationDto dto) {
        ReservationEntity e = toEntity(dto);
        e.setReservationId(null);
        return toDto(repository.save(e));
    }

    public ReservationDto update(Integer id, ReservationDto dto) {
        ReservationEntity existing = repository.findById(id).orElseThrow(() -> new NotFoundException("No se encontró reserva con id: " + id));
        existing.setBookId(dto.getBookId());
        existing.setUserId(dto.getUserId());
        existing.setStartDate(LocalDate.parse(dto.getStartDate()));
        existing.setEndDate(LocalDate.parse(dto.getEndDate()));
        return toDto(repository.save(existing));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("No se encontró reserva con id: " + id);
        }
        repository.deleteById(id);
    }

    public List<ReservationDto> findByUser(Integer userId) {
        return repository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ReservationDto> findByBookNative(Integer bookId) {
        return repository.findByBookIdNative(bookId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ReservationDto> findByDateRange(String start, String end) {
        return repository.findByDateRange(LocalDate.parse(start), LocalDate.parse(end))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private ReservationDto toDto(ReservationEntity e) {
        ReservationDto d = new ReservationDto();
        d.setReservationId(e.getReservationId());
        d.setBookId(e.getBookId());
        d.setUserId(e.getUserId());
        d.setStartDate(e.getStartDate().toString());
        d.setEndDate(e.getEndDate().toString());
        return d;
    }

    private ReservationEntity toEntity(ReservationDto d) {
        ReservationEntity e = new ReservationEntity();
        e.setReservationId(d.getReservationId());
        e.setBookId(d.getBookId());
        e.setUserId(d.getUserId());
        e.setStartDate(LocalDate.parse(d.getStartDate()));
        e.setEndDate(LocalDate.parse(d.getEndDate()));
        return e;
    }
}
