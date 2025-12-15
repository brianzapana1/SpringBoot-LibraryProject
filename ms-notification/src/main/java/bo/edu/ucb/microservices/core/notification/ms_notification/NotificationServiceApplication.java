package bo.edu.ucb.microservices.core.notification.ms_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
        "bo.edu.ucb.microservices.core.notification.ms_notification",
        "bo.edu.ucb.microservices.util"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
