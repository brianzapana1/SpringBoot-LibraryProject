package com.library.reservation.controller;

import com.library.reservation.ReservationDto;
import bo.edu.ucb.microservices.util.exceptions.InvalidInputException;
import bo.edu.ucb.microservices.util.http.HttpErrorInfo;
import bo.edu.ucb.microservices.util.http.ServiceUtil;
import com.library.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/v1/reservation")
@Tag(name = "Reservation", description = "REST API para reservas")
public class ReservationServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationServiceController.class);
    private final ServiceUtil serviceUtil;
    private final ReservationService reservationService;

    @Autowired
    public ReservationServiceController(ServiceUtil serviceUtil, ReservationService reservationService) {
        this.serviceUtil = serviceUtil;
        this.reservationService = reservationService;
    }

    @Operation(summary = "Obtiene una reserva por su ID", description = "Retorna los datos de una reserva específica según su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada correctamente", content = @Content(schema = @Schema(implementation = ReservationDto.class))),
            @ApiResponse(responseCode = "400", description = "ID de reserva inválido", content = @Content(schema = @Schema(implementation = HttpErrorInfo.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content(schema = @Schema(implementation = HttpErrorInfo.class)))
    })
    @PreAuthorize("hasAnyRole('user', 'admin')")
    @GetMapping(value = "/{reservationId}", produces = "application/json")
    public Mono<ResponseEntity<ReservationDto>> getReservation(
            @Parameter(description = "ID de la reserva a obtener", required = true)
            @PathVariable("reservationId") int reservationId) {
        LOGGER.info("Obteniendo reserva por el id: {}", reservationId);
        if (reservationId < 1) {
            return Mono.error(new InvalidInputException("Id de reserva inválido: " + reservationId));
        }
        return Mono.just(ResponseEntity.ok(reservationService.getById(reservationId)));
    }

    @Operation(summary = "Crea una nueva reserva", description = "Crea una nueva reserva en el sistema.")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<ReservationDto>> createReservation(@RequestBody(required = true) ReservationDto dto) {
        LOGGER.info("Creando reserva: {}", dto);
        try {
            validatePayload(dto);
            return Mono.just(ResponseEntity.ok(reservationService.create(dto)));
        } catch (InvalidInputException e) {
            return Mono.error(e);
        }
    }

    @Operation(summary = "Actualiza una reserva existente", description = "Actualiza los datos de una reserva específica según su ID.")
    @PreAuthorize("hasAnyRole('admin', 'manager')")
    @PutMapping(value = "/{reservationId}", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<ReservationDto>> updateReservation(
            @PathVariable("reservationId") int reservationId,
            @RequestBody(required = true) ReservationDto dto) {
        LOGGER.info("Actualizando reserva con id: {}", reservationId);
        if (reservationId < 1) {
            return Mono.error(new InvalidInputException("Id de reserva inválido: " + reservationId));
        }
        try {
            validatePayload(dto);
            return Mono.just(ResponseEntity.ok(reservationService.update(reservationId, dto)));
        } catch (InvalidInputException e) {
            return Mono.error(e);
        }
    }

    @Operation(summary = "Elimina una reserva", description = "Elimina una reserva específica según su ID.")
    @PreAuthorize("hasAnyRole('admin', 'manager')")
    @DeleteMapping(value = "/{reservationId}", produces = "application/json")
    public Mono<ResponseEntity<String>> deleteReservation(@PathVariable("reservationId") int reservationId) {
        LOGGER.info("Eliminando reserva con id: {}", reservationId);
        if (reservationId < 1) {
            return Mono.error(new InvalidInputException("Id de reserva inválido: " + reservationId));
        }
        reservationService.delete(reservationId);
        String message = "Reserva con id " + reservationId + " eliminada correctamente.";
        return Mono.just(ResponseEntity.ok(message));
    }

    // ---------- CONSULTAS PARA EVIDENCIA ----------

    @Operation(summary = "Buscar por usuario (Derived Query)", description = "Usa un método derivado findByUserId.")
    @GetMapping(value = "/user/{userId}", produces = "application/json")
    public Mono<ResponseEntity<List<ReservationDto>>> findByUser(@PathVariable("userId") int userId) {
        if (userId < 1) return Mono.error(new InvalidInputException("userId inválido: " + userId));
        return Mono.just(ResponseEntity.ok(reservationService.findByUser(userId)));
    }

    @Operation(summary = "Buscar por libro (Native Query)", description = "Usa una consulta nativa por bookId.")
    @GetMapping(value = "/book/{bookId}", produces = "application/json")
    public Mono<ResponseEntity<List<ReservationDto>>> findByBook(@PathVariable("bookId") int bookId) {
        if (bookId < 1) return Mono.error(new InvalidInputException("bookId inválido: " + bookId));
        return Mono.just(ResponseEntity.ok(reservationService.findByBookNative(bookId)));
    }

    @Operation(summary = "Buscar por rango de fechas (JPQL/Criteria)", description = "Devuelve reservas entre start y end (yyyy-MM-dd).")
    @GetMapping(value = "/date-range", produces = "application/json")
    public Mono<ResponseEntity<List<ReservationDto>>> findByDateRange(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            return Mono.error(new InvalidInputException("Parámetros start y end son obligatorios (yyyy-MM-dd)"));
        }
        try {
            LocalDate.parse(start);
            LocalDate.parse(end);
        } catch (DateTimeParseException ex) {
            return Mono.error(new InvalidInputException("Formato de fecha inválido. Use yyyy-MM-dd"));
        }
        return Mono.just(ResponseEntity.ok(reservationService.findByDateRange(start, end)));
    }

    private void validatePayload(ReservationDto dto) {
        if (dto == null) {
            throw new InvalidInputException("El cuerpo de la solicitud no puede ser nulo");
        }
        if (dto.getBookId() == null || dto.getBookId() < 1) {
            throw new InvalidInputException("bookId es obligatorio y debe ser mayor o igual a 1");
        }
        if (dto.getUserId() == null || dto.getUserId() < 1) {
            throw new InvalidInputException("userId es obligatorio y debe ser mayor o igual a 1");
        }
        if (dto.getStartDate() == null || dto.getStartDate().isBlank()) {
            throw new InvalidInputException("startDate es obligatorio (formato yyyy-MM-dd)");
        }
        if (dto.getEndDate() == null || dto.getEndDate().isBlank()) {
            throw new InvalidInputException("endDate es obligatorio (formato yyyy-MM-dd)");
        }
        try {
            LocalDate start = LocalDate.parse(dto.getStartDate());
            LocalDate end = LocalDate.parse(dto.getEndDate());
            if (end.isBefore(start)) {
                throw new InvalidInputException("endDate no puede ser anterior a startDate");
            }
        } catch (DateTimeParseException ex) {
            throw new InvalidInputException("Formato de fecha inválido. Use yyyy-MM-dd");
        }
    }
}
