package bo.edu.ucb.microservices.util.http;

import java.time.ZonedDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(name = "HttpErrorInfo", description = "Estructura estándar para representar errores HTTP en la API")
public class HttpErrorInfo {

    @Schema(description = "Marca de tiempo cuando ocurrió el error", example = "2025-08-29T23:15:30Z")
    private final ZonedDateTime timestamp;

    @Schema(description = "Ruta o endpoint de la petición que generó el error", example = "/api/v1/libros/123")
    private final String path;

    @Schema(description = "Código de estado HTTP asociado al error", example = "404")
    private final HttpStatus httpStatus;

    @Schema(description = "Mensaje detallado del error", example = "El recurso solicitado no existe")
    private final String message;

    public HttpErrorInfo() {
        timestamp = null;
        this.httpStatus = null;
        this.path = null;
        this.message = null;
    }

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        timestamp = ZonedDateTime.now();
        this.httpStatus = httpStatus;
        this.path = path;
        this.message = message;
    }

    @Schema(description = "Obtiene la fecha y hora exacta en que ocurrió el error")
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Schema(description = "Obtiene la ruta de la petición que causó el error")
    public String getPath() {
        return path;
    }

    @Schema(description = "Obtiene el código de estado HTTP en formato numérico", example = "400")
    public int getStatus() {
        return httpStatus.value();
    }

    @Schema(description = "Obtiene la descripción textual del estado HTTP", example = "Bad Request")
    public String getError() {
        return httpStatus.getReasonPhrase();
    }

    @Schema(description = "Obtiene el mensaje asociado al error")
    public String getMessage() {
        return message;
    }
}
