package com.micriservice.loan.ms_loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Petición para crear un nuevo préstamo de libro")
public class CreateLoanRequest {

    @NotNull @Positive
    @Schema(description = "ID del usuario que solicita el préstamo", example = "1")
    private Long userId;

    @NotNull @Positive
    @Schema(description = "ID de la copia del libro a prestar", example = "5")
    private Long copyId;

    @NotNull
    @Schema(description = "Fecha de inicio del préstamo (YYYY-MM-DD)",
            example = "2025-10-25")
    private LocalDate loanDate;

    @NotNull @Future
    @Schema(description = "Fecha límite de devolución (YYYY-MM-DD, debe ser futura)",
            example = "2025-11-02")
    private LocalDate dueDate;
}