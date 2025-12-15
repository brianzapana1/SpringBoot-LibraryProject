package bo.edu.ucb.microservices.core.book.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = false)
public class SecurityConfig {

        private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

        @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:http://keycloak:8080/realms/library-realm/protocol/openid-connect/certs}")
        private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                // CORS disabled here - handled exclusively by Gateway to prevent duplicate headers
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/ms-book/v1/api/health", "/api/health", "/health").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers("/swagger-ui.html").permitAll()
                        // Protected endpoints
                        .pathMatchers("/ms-book/v1/api/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    // CORS configuration removed - handled by Gateway to prevent duplicate headers

        @Bean
    @ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = false)
    public ReactiveJwtDecoder jwtDecoder() {
                String resolvedJwkSetUri = resolveInternalKeycloakEndpoint(jwkSetUri);
                return NimbusReactiveJwtDecoder.withJwkSetUri(resolvedJwkSetUri).build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = false)
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("realm_access.roles");

        ReactiveJwtAuthenticationConverter authenticationConverter = new ReactiveJwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(authoritiesConverter)
        );
        
        return authenticationConverter;
    }

        private String resolveInternalKeycloakEndpoint(String configuredUri) {
                try {
                        URI original = URI.create(configuredUri);
                        String host = original.getHost();
                        int port = original.getPort();

                        if (host == null) {
                                return configuredUri;
                        }

                        boolean needsContainerHost = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
                        boolean usesExternalKeycloakPort = "keycloak".equals(host) && port == 8180;

                        if (needsContainerHost || usesExternalKeycloakPort) {
                                host = "keycloak";
                                port = 8080;
                        } else if ("keycloak".equals(host) && port == -1) {
                                // Default Keycloak container port
                                port = 8080;
                        }

                        URI adjusted = new URI(
                                        original.getScheme(),
                                        original.getUserInfo(),
                                        host,
                                        port,
                                        original.getPath(),
                                        original.getQuery(),
                                        original.getFragment()
                        );

                        String resolved = adjusted.toString();
                        if (!resolved.equals(configuredUri)) {
                                LOG.info("Resolved Keycloak JWKS endpoint from '{}' to '{}'", configuredUri, resolved);
                        }
                        return resolved;
                } catch (URISyntaxException | IllegalArgumentException ex) {
                        LOG.error("Invalid JWKS URI '{}', falling back to container default", configuredUri, ex);
                        return "http://keycloak:8080/realms/library-realm/protocol/openid-connect/certs";
                }
        }
}