package com.library.reservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // El token puede tener issuer "http://localhost:8090/realms/library-realm"
        // pero necesitamos acceder a Keycloak vía "http://host.docker.internal:8090"
        // Usamos withJwkSetUri directamente para evitar la validación automática del issuer
        String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
        
        LOGGER.info("Configuring JWT decoder with JWK Set URI: {}", jwkSetUri);
        LOGGER.info("Expected issuer URI: {}", issuerUri);
        
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();
        
        // Envolver el decoder para aceptar tokens con issuer localhost:8090 o host.docker.internal:8090
        return new ReactiveJwtDecoder() {
            @Override
            public Mono<Jwt> decode(String token) {
                LOGGER.info("Decoding JWT token...");
                return decoder.decode(token)
                        .doOnNext(jwt -> {
                            String tokenIssuer = jwt.getIssuer().toString();
                            LOGGER.info("Token decoded successfully. Issuer: {}", tokenIssuer);
                            LOGGER.info("Token claims: {}", jwt.getClaims().keySet());
                            // Aceptar tokens con issuer localhost:8090 o host.docker.internal:8090
                            // Solo validamos que contenga library-realm
                            if (!tokenIssuer.contains("library-realm")) {
                                LOGGER.error("Invalid issuer in token: {}. Expected to contain 'library-realm'", tokenIssuer);
                                throw new JwtException("Invalid issuer: " + tokenIssuer);
                            }
                            LOGGER.info("Token issuer validation passed");
                        })
                        .doOnError(error -> {
                            LOGGER.error("Error decoding JWT token: {}", error.getMessage(), error);
                        });
            }
        };
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/openapi/**", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtDecoder(jwtDecoder())
                    .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter()))
                )
            );
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        try {
            LOGGER.info("Extracting authorities from JWT. Issuer: {}", jwt.getIssuer());
            LOGGER.info("JWT claims: {}", jwt.getClaims().keySet());
            
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            LOGGER.info("realm_access claim: {}", realmAccess);
            
            if (realmAccess != null) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                LOGGER.info("Roles from token: {}", roles);
                
                if (roles != null && !roles.isEmpty()) {
                    List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> {
                            String authority = "ROLE_" + role;
                            LOGGER.info("Adding authority: {}", authority);
                            return new SimpleGrantedAuthority(authority);
                        })
                        .collect(Collectors.toList());
                    LOGGER.info("Extracted {} authorities from JWT: {}", authorities.size(), authorities);
                    return authorities;
                }
            }
            LOGGER.warn("No roles found in JWT token. realm_access claim: {}", realmAccess);
            // Si no hay roles, devolvemos una lista vacía pero logueamos para debugging
            return List.of();
        } catch (Exception e) {
            LOGGER.error("Error extracting authorities from JWT", e);
            // En caso de error, devolvemos lista vacía para que no falle la autenticación
            // pero el @PreAuthorize fallará si no hay roles
            return List.of();
        }
    }
}

