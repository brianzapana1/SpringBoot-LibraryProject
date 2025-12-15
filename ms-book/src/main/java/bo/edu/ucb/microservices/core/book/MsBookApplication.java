package bo.edu.ucb.microservices.core.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
    ReactiveSecurityAutoConfiguration.class,
    ReactiveUserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration.class
})
@EnableDiscoveryClient // Enabled for Eureka service discovery
@ComponentScan({"bo.edu.ucb.microservices.core.book", "bo.edu.ucb.microservices.util"})
public class MsBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsBookApplication.class, args);
	}
}