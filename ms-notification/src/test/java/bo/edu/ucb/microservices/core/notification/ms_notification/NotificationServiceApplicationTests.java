package bo.edu.ucb.microservices.core.notification.ms_notification;

import bo.edu.ucb.microservices.dto.notification.NotificationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class NotificationServiceApplicationTests {

    @Autowired
    private WebTestClient client;

    private static final int NOTIFICATION_ID_OK = 1;
    private static final int NOTIFICATION_ID_NOT_FOUND = 13;
    private static final int NOTIFICATION_ID_INVALID = -1;

    // --- GET OK ---
    @Test
    void getNotificationById_OK() {
        client.get()
                .uri("/v1/notification/" + NOTIFICATION_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.notificationId").isEqualTo(NOTIFICATION_ID_OK)
                .jsonPath("$.title").isEqualTo("Título de prueba " + NOTIFICATION_ID_OK)
                .jsonPath("$.message").isEqualTo("Este es un mensaje de prueba para la notificación con id " + NOTIFICATION_ID_OK);
    }

    // --- GET NOT FOUND ---
    @Test
    void getNotificationById_NotFound() {
        client.get()
                .uri("/v1/notification/" + NOTIFICATION_ID_NOT_FOUND)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("No se encontró notificación con id: " + NOTIFICATION_ID_NOT_FOUND);
    }

    // --- GET INVALID INPUT ---
    @Test
    void getNotificationById_InvalidInput() {
        client.get()
                .uri("/v1/notification/" + NOTIFICATION_ID_INVALID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422) // <-- Cambiado a 422 UNPROCESSABLE_ENTITY
                .expectBody()
                .jsonPath("$.message").isEqualTo("Id de notificación inválido: " + NOTIFICATION_ID_INVALID);
    }


    // --- POST OK ---
    @Test
    void createNotification_OK() {
        NotificationDto request = new NotificationDto(0, "Nuevo título", "Nuevo mensaje");

        client.post()
                .uri("/v1/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.notificationId").isNumber()
                .jsonPath("$.title").isEqualTo("Nuevo título")
                .jsonPath("$.message").isEqualTo("Nuevo mensaje");
    }

    // --- PUT OK ---
    @Test
    void updateNotification_OK() {
        NotificationDto request = new NotificationDto(0, "Título actualizado", "Mensaje actualizado");

        client.put()
                .uri("/v1/notification/" + NOTIFICATION_ID_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.notificationId").isEqualTo(NOTIFICATION_ID_OK)
                .jsonPath("$.title").isEqualTo("Título actualizado")
                .jsonPath("$.message").isEqualTo("Mensaje actualizado");
    }

    // --- DELETE OK ---
    @Test
    void deleteNotification_OK() {
        client.delete()
                .uri("/v1/notification/" + NOTIFICATION_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Notificación con id " + NOTIFICATION_ID_OK + " eliminada correctamente.");
    }
}
