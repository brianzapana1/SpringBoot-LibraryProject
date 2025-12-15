package bo.edu.ucb.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gateway")
public class GatewayInfoController {

    @Autowired
    private RouteLocator routeLocator;

    @GetMapping("/info")
    public Mono<Map<String, Object>> getGatewayInfo() {
        return Mono.just(Map.of(
                "gateway_name", "Library Management System Gateway",
                "version", "1.0.0",
                "description", "Edge Server for Library Microservices",
                "timestamp", LocalDateTime.now(),
                "status", "active",
                "features", Map.of(
                        "routing", "enabled",
                        "load_balancing", "enabled",
                        "circuit_breaker", "enabled",
                        "swagger_integration", "enabled",
                        "eureka_discovery", "enabled"
                )
        ));
    }

    @GetMapping("/routes")
    public Mono<Map<String, Object>> getActiveRoutes() {
        return routeLocator.getRoutes()
                .collectList()
                .map(routes -> Map.of(
                        "total_routes", routes.size(),
                        "timestamp", LocalDateTime.now(),
                        "routes", routes.stream()
                                .collect(Collectors.toMap(
                                        route -> route.getId(),
                                        route -> Map.of(
                                                "uri", route.getUri().toString(),
                                                "metadata", route.getMetadata()
                                        )
                                ))
                ));
    }

    @GetMapping("/health")
    public Mono<Map<String, Object>> getGatewayHealth() {
        return Mono.just(Map.of(
                "status", "UP",
                "gateway", "operational",
                "timestamp", LocalDateTime.now(),
                "services", Map.of(
                        "discovery_client", "connected",
                        "route_locator", "active",
                        "load_balancer", "enabled"
                ),
                "endpoints", Map.of(
                        "swagger_ui", "/swagger-ui.html",
                        "api_docs", "/v3/api-docs",
                        "actuator", "/actuator",
                        "ms_book_api", "/ms-book/v1/api/**",
                        "direct_books_api", "/api/books/**"
                )
        ));
    }
}