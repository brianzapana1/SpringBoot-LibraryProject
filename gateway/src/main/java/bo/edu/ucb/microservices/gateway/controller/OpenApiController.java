package bo.edu.ucb.microservices.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/openapi")
public class OpenApiController {

    @Autowired
    private RouteLocator routeLocator;

    @GetMapping("/swagger-config")
    public ResponseEntity<Map<String, Object>> swaggerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("url", "/openapi/v3/api-docs");
        config.put("urls", new Object[]{
            Map.of("url", "/openapi/v3/api-docs", "name", "Gateway API"),
            Map.of("url", "/ms-book/v3/api-docs", "name", "MS-Book API")
        });
        return ResponseEntity.ok(config);
    }

    @GetMapping("/v3/api-docs")
    public ResponseEntity<Map<String, Object>> apiDocs() {
        Map<String, Object> apiDocs = new HashMap<>();
        apiDocs.put("openapi", "3.0.1");
        
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Gateway API");
        info.put("description", "API Gateway for Microservices");
        info.put("version", "1.0.0");
        apiDocs.put("info", info);
        
        Map<String, Object> paths = new HashMap<>();
        
        // Add Gateway routes information
        Map<String, Object> booksPath = new HashMap<>();
        Map<String, Object> getBooks = new HashMap<>();
        getBooks.put("summary", "Get all books through Gateway");
        getBooks.put("operationId", "getAllBooksGateway");
        getBooks.put("tags", new String[]{"Books via Gateway"});
        
        Map<String, Object> responses = new HashMap<>();
        responses.put("200", Map.of("description", "Successful response"));
        getBooks.put("responses", responses);
        
        booksPath.put("get", getBooks);
        paths.put("/api/books", booksPath);
        paths.put("/ms-book/v1/api/books", booksPath);
        
        apiDocs.put("paths", paths);
        
        return ResponseEntity.ok(apiDocs);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Gateway");
        return ResponseEntity.ok(health);
    }
}