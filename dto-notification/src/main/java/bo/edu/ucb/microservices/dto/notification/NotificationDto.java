package bo.edu.ucb.microservices.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO que representa una notificación dentro del sistema")
public class NotificationDto {

    @Schema(description = "ID único de la notificación", example = "1")
    private int notificationId;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    @Schema(description = "Título de la notificación", example = "Recordatorio de devolución")
    @JsonProperty("title") // 👈 Este es el nombre que aparecerá en el JSON
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 5, max = 500, message = "El mensaje debe tener entre 5 y 500 caracteres")
    @Schema(description = "Contenido o mensaje de la notificación", example = "Recuerde devolver el libro antes del 30/08/2025")
    @JsonProperty("message") // 👈 Este es el nombre que aparecerá en el JSON
    private String mensaje;

    public NotificationDto(int notificationId, String titulo, String mensaje) {
        this.notificationId = notificationId;
        this.titulo = titulo;
        this.mensaje = mensaje;
    }

    public NotificationDto() {
        // Constructor vacío requerido por frameworks como Jackson
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
