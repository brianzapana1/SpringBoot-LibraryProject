package bo.edu.ucb.microservices.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO que representa una notificación dentro del sistema")
public class NotificationDto {

    @Schema(description = "ID único de la notificación", example = "1")
    private int notificationId;

    @Schema(description = "Título de la notificación", example = "Recordatorio de devolución")
    private String titulo;

    @Schema(description = "Contenido o mensaje de la notificación", example = "Recuerde devolver el libro antes del 30/08/2025")
    private String mensaje;

    public NotificationDto(int notificationId, String titulo, String mensaje) {
        this.notificationId = notificationId;
        this.titulo = titulo;
        this.mensaje = mensaje;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "NotificationDto [notificationId=" + notificationId + ", titulo=" + titulo + ", mensaje=" + mensaje + "]";
    }
}
