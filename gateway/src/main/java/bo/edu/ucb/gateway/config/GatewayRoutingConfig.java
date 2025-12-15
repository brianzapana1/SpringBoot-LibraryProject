package bo.edu.ucb.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutingConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Direct Books API routing - FIRST ROUTE (highest priority by default)
                .route("books-direct-api", r -> r
                        .path("/api/books/**")
                        .and()
                        .method("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .filters(f -> f
                                .rewritePath("/api/books(?<segment>.*)", "/ms-book/v1/api/books$\\{segment}")
                                .addRequestHeader("X-API-Version", "v1")
                                .addRequestHeader("X-Gateway-Route", "true")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // MS-Book API Routes - Pass through without modification
                .route("ms-book-api-v1", r -> r
                        .path("/ms-book/v1/api/**")
                        .and()
                        .method("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway", "Spring-Cloud-Gateway")
                                .addRequestHeader("X-Request-Source", "API-Gateway")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // API Search endpoints routing
                .route("api-search", r -> r
                        .path("/api/search/**")
                        .filters(f -> f
                                .rewritePath("/api/search(?<segment>.*)", "/ms-book/v1/api/search$\\{segment}")
                                .addRequestHeader("X-API-Version", "v1")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // API Authors endpoint routing
                .route("api-authors", r -> r
                        .path("/api/authors")
                        .filters(f -> f
                                .setPath("/ms-book/v1/api/authors")
                                .addRequestHeader("X-API-Version", "v1")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // API Available books endpoint routing
                .route("api-available", r -> r
                        .path("/api/available")
                        .filters(f -> f
                                .setPath("/ms-book/v1/api/available")
                                .addRequestHeader("X-API-Version", "v1")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // API Statistics endpoint routing
                .route("api-statistics", r -> r
                        .path("/api/statistics/**")
                        .filters(f -> f
                                .rewritePath("/api/statistics(?<segment>.*)", "/ms-book/v1/api/statistics$\\{segment}")
                                .addRequestHeader("X-API-Version", "v1")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // API Popular books endpoint routing
                .route("api-popular", r -> r
                        .path("/api/popular")
                        .filters(f -> f
                                .setPath("/ms-book/v1/api/popular")
                                .addRequestHeader("X-API-Version", "v1")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // API Health and Info endpoints routing
                .route("api-health-info", r -> r
                        .path("/api/health", "/api/info")
                        .filters(f -> f
                                .rewritePath("/api(?<segment>.*)", "/ms-book/v1/api$\\{segment}")
                                .addRequestHeader("X-API-Version", "v1")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // Discovery endpoints
                .route("ms-book-discovery", r -> r
                        .path("/ms-book/discovery/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addResponseHeader("X-Gateway-Discovery", "enabled")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // MS-Book OpenAPI/Swagger documentation proxy
                .route("ms-book-api-docs", r -> r
                        .path("/ms-book/v3/api-docs", "/ms-book/v3/api-docs/**")
                        .filters(f -> f
                                .stripPrefix(1)  // Remove /ms-book prefix
                                .addResponseHeader("Access-Control-Allow-Origin", "*")
                                .addResponseHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
                                .addResponseHeader("Access-Control-Allow-Headers", "*")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // MS-Book Swagger UI resources
                .route("ms-book-swagger-ui", r -> r
                        .path("/ms-book/swagger-ui/**", "/ms-book/swagger-ui.html")
                        .filters(f -> f
                                .stripPrefix(1)  // Remove /ms-book prefix
                                .addResponseHeader("Access-Control-Allow-Origin", "*")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // Gateway's own Swagger and API Documentation
                .route("gateway-swagger", r -> r
                        .path("/swagger-ui/**", "/webjars/**", "/v3/api-docs/**", "/swagger-ui.html")
                        .filters(f -> f
                                .addResponseHeader("Access-Control-Allow-Origin", "*")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // Actuator endpoints with authentication check
                .route("ms-book-actuator", r -> r
                        .path("/ms-book/actuator/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Actuator-Gateway", "true")
                        )
                        .uri("http://ms-book:8082")
                )
                
                // Fallback route for undefined endpoints
                .route("fallback-route", r -> r
                        .path("/**")
                        .and()
                        .predicate(exchange -> {
                            String path = exchange.getRequest().getPath().value();
                            return !path.startsWith("/actuator") && 
                                   !path.startsWith("/swagger-ui") && 
                                   !path.startsWith("/v3/api-docs") &&
                                   !path.startsWith("/ms-book/v3/api-docs") &&
                                   !path.startsWith("/ms-book/swagger-ui");
                        })
                        .filters(f -> f
                                .setStatus(404)
                                .addResponseHeader("X-Gateway-Error", "Route-Not-Found")
                        )
                        .uri("http://ms-book:8082")
                )
                
                .build();
    }
}