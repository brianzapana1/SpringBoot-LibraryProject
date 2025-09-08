package bo.edu.ucb.microservices.core.notification.ms_notification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import bo.edu.ucb.microservices.dto.notification.NotificationDto;
import bo.edu.ucb.microservices.util.exceptions.InvalidInputException;
import bo.edu.ucb.microservices.util.exceptions.NotFoundException;
import bo.edu.ucb.microservices.util.http.HttpErrorInfo;
import bo.edu.ucb.microservices.util.http.ServiceUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/v1/notification")
@Tag(name = "Notification", description = "REST API para notificaciones")
public class NotificationServiceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceController.class);
    private final ServiceUtil serviceUtil;

    @Autowired
    public NotificationServiceController(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    // ------------------ GET ------------------
    @Operation(summary = "Obtiene una notificación por su ID",
            description = "Retorna los datos de una notificación específica según su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación encontrada correctamente",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class))),
            @ApiResponse(responseCode = "400", description = "ID de notificación inválido",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class))),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class)))
    })
    @GetMapping(value = "/{notificationId}", produces = "application/json")
    public ResponseEntity<NotificationDto> getNotification(
            @Parameter(description = "ID de la notificación a obtener", required = true)
            @PathVariable("notificationId") int notificationId
    ) {
        LOGGER.info("Obteniendo notificación por el id: {}", notificationId);

        if (notificationId < 1) {
            throw new InvalidInputException("Id de notificación inválido: " + notificationId);
        }

        if (notificationId == 13) {
            throw new NotFoundException("No se encontró notificación con id: " + notificationId);
        }

        NotificationDto dto = new NotificationDto(
                notificationId,
                "Título de prueba " + notificationId,
                "Este es un mensaje de prueba para la notificación con id " + notificationId
        );

        return ResponseEntity.ok(dto);
    }

    // ------------------ POST ------------------
    @Operation(summary = "Crea una nueva notificación",
            description = "Crea una nueva notificación en el sistema.")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<NotificationDto> createNotification(@RequestBody NotificationDto dto) {
        LOGGER.info("Creando notificación: {}", dto);

        // Simula la creación asignando un ID
        dto.setNotificationId((int) (Math.random() * 1000));
        return ResponseEntity.ok(dto);
    }

    // ------------------ PUT ------------------
    @Operation(summary = "Actualiza una notificación existente",
            description = "Actualiza los datos de una notificación específica según su ID.")
    @PutMapping(value = "/{notificationId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<NotificationDto> updateNotification(
            @PathVariable("notificationId") int notificationId,
            @RequestBody NotificationDto dto
    ) {
        LOGGER.info("Actualizando notificación con id: {}", notificationId);

        dto.setNotificationId(notificationId);
        return ResponseEntity.ok(dto);
    }

    // ------------------ DELETE ------------------
    @Operation(summary = "Elimina una notificación",
            description = "Elimina una notificación específica según su ID.")
    @DeleteMapping(value = "/{notificationId}", produces = "application/json")
    public ResponseEntity<String> deleteNotification(@PathVariable("notificationId") int notificationId) {
        LOGGER.info("Eliminando notificación con id: {}", notificationId);

        // Simulación de eliminación exitosa
        String message = "Notificación con id " + notificationId + " eliminada correctamente.";
        return ResponseEntity.ok(message);
    }
}
