package com.micriservice.loan.ms_loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Petición para devolver un préstamo existente")
public class ReturnLoanRequest {

    @Schema(
            description = "Fecha en que se devuelve el libro (opcional). Si no se envía, se usa la fecha actual.",
            example = "2025-10-29"
    )
    private LocalDate returnedAt;
}