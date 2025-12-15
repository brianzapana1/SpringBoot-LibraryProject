package bo.edu.ucb.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/ms-book")
    public Mono<Map<String, Object>> msBookFallback() {
        return Mono.just(Map.of(
                "error", "Service Temporarily Unavailable",
                "message", "MS-Book service is currently down. Please try again later.",
                "service", "MS-BOOK",
                "timestamp", LocalDateTime.now(),
                "gateway", "Spring Cloud Gateway",
                "fallback", true
        ));
    }

    @GetMapping("/general")
    public Mono<Map<String, Object>> generalFallback() {
        return Mono.just(Map.of(
                "error", "Service Error",
                "message", "The requested service is temporarily unavailable.",
                "timestamp", LocalDateTime.now(),
                "gateway", "Spring Cloud Gateway",
                "fallback", true
        ));
    }
}