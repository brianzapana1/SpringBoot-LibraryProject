package bo.edu.ucb.microservices.core.book.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msBookOpenAPI() {
        // Define Bearer JWT Security Scheme
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Token JWT de Keycloak. Formato: Bearer {token}");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("Gateway"),
                        new Server().url("http://localhost:8082").description("MS-Book Direct")
                ))
                .info(new Info()
                        .title("MS-Book Service API")
                        .description("Microservicio para gestión de libros del sistema de biblioteca.\n\n" +
                                "## Autenticación\n" +
                                "Esta API utiliza JWT Bearer tokens emitidos por Keycloak.\n\n" +
                                "### Obtener Token:\n" +
                                "```bash\n" +
                                "curl -X POST http://localhost:8180/realms/library-realm/protocol/openid-connect/token \\\n" +
                                "  -d \"grant_type=password\" \\\n" +
                                "  -d \"client_id=library-client\" \\\n" +
                                "  -d \"username=admin\" \\\n" +
                                "  -d \"password=admin\"\n" +
                                "```\n\n" +
                                "### Usar Token:\n" +
                                "Haz clic en **Authorize** y pega el token (sin 'Bearer ').")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo UCB")
                                .email("desarrollo@ucb.edu.bo")
                                .url("https://www.ucb.edu.bo"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
