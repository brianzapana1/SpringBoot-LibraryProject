package com.micriservice.loan.ms_loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Respuesta con la información de un préstamo")
public class LoanResponse {

    @Schema(description = "ID del préstamo generado", example = "100")
    private Long id;

    @Schema(description = "ID del usuario asociado al préstamo", example = "1")
    private Long userId;

    @Schema(description = "ID de la copia del libro asociado al préstamo", example = "5")
    private Long copyId;

    @Schema(description = "Fecha de inicio del préstamo (YYYY-MM-DD)", example = "2025-10-25")
    private LocalDate loanDate;

    @Schema(description = "Fecha límite de devolución (YYYY-MM-DD)", example = "2025-11-02")
    private LocalDate dueDate;

    @Schema(description = "Fecha en que fue devuelto el libro, si aplica", example = "2025-10-29")
    private LocalDate returnedAt;

    @Schema(description = "Estado actual del préstamo (OPEN o CLOSED)", example = "OPEN")
    private String status;

    @Schema(description = "Indica si el préstamo está atrasado", example = "false")
    private boolean late;

    @Schema(description = "Número de días entre préstamo y vencimiento", example = "7")
    private int days;
}