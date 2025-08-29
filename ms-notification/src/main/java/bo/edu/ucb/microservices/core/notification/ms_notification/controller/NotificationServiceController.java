package bo.edu.ucb.microservices.core.notification.ms_notification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(
            summary = "Obtiene una notificación por su ID",
            description = "Retorna los datos de una notificación específica según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación encontrada correctamente",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class))),
            @ApiResponse(responseCode = "400", description = "ID de notificación inválido",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class))),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada",
                    content = @Content(schema = @Schema(implementation = HttpErrorInfo.class)))
    })
    @GetMapping(value = "/{notificationId}", produces = "application/json")
    public NotificationDto getNotification(
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

        return new NotificationDto(
                notificationId,
                "name-" + notificationId,
                serviceUtil.getServiceAddress()
        );
    }
}
