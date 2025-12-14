package com.micriservice.loan.ms_loan.controller;

import com.micriservice.loan.ms_loan.dto.CreateLoanRequest;
import com.micriservice.loan.ms_loan.dto.ReturnLoanRequest;
import com.micriservice.loan.ms_loan.dto.LoanResponse;
import com.micriservice.loan.ms_loan.model.Loan;
import com.micriservice.loan.ms_loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Tag(name = "Loans", description = "Gestión de préstamos de libros")
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService service;
    public LoanController(LoanService service) { this.service = service; }

    @Operation(summary = "Crear préstamo")
    @PostMapping
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody CreateLoanRequest dto) {
        Loan loan = service.create(
                dto.getUserId(),
                dto.getCopyId(),     // <- antes era bookId
                dto.getLoanDate(),   // <- antes startAt (LocalDateTime)
                dto.getDueDate()     // <- antes dueAt (LocalDateTime)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(loan));
    }

    @Operation(summary = "Obtener préstamo por ID")
    @GetMapping("/{id}")
    public LoanResponse get(@PathVariable Long id) {
        return toResponse(service.get(id));
    }

    @Operation(summary = "Devolver préstamo (idempotente)")
    @PatchMapping("/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id,
                                   @RequestBody(required = false) ReturnLoanRequest body) {
        LocalDate returnedAt = (body != null ? body.getReturnedAt() : null);
        return toResponse(service.returnLoan(id, returnedAt));
    }

    @Operation(summary = "Listar préstamos abiertos por usuario (Derived Query)")
    @GetMapping("/user/{userId}")
    public List<LoanResponse> getLoansByUser(@PathVariable Long userId) {
        return service.getLoansByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(summary = "Listar préstamos vencidos (Native Query)")
    @GetMapping("/overdue")
    public List<LoanResponse> getOverdueLoans() {
        return service.getOverdueLoans()
                .stream()
                .map(this::toResponse)
                .toList();
    }



    /** Mapea entidad -> DTO de respuesta */
    private LoanResponse toResponse(Loan l) {
        LoanResponse r = new LoanResponse();
        r.setId(l.getId());
        r.setUserId(l.getUser().getId());
        r.setCopyId(l.getCopy().getId());
        r.setLoanDate(l.getLoanDate());
        r.setDueDate(l.getDueDate());
        r.setReturnedAt(l.getReturnedAt());

        boolean open = (l.getReturnedAt() == null);
        boolean late = open && LocalDate.now().isAfter(l.getDueDate());
        r.setStatus(open ? "OPEN" : "CLOSED");
        r.setLate(late);
        r.setDays((int) ChronoUnit.DAYS.between(l.getLoanDate(), l.getDueDate()));
        return r;
    }
}