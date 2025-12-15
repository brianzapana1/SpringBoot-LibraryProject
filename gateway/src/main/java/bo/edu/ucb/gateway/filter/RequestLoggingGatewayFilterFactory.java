package bo.edu.ucb.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class RequestLoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestLoggingGatewayFilterFactory.Config> {

    public RequestLoggingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String requestId = java.util.UUID.randomUUID().toString();
            String startTime = Instant.now().toString();
            
            // Log request
            System.out.println("🚀 GATEWAY REQUEST [" + requestId + "]");
            System.out.println("   📋 Method: " + exchange.getRequest().getMethod());
            System.out.println("   🔗 Path: " + exchange.getRequest().getPath());
            System.out.println("   🕒 Time: " + startTime);
            System.out.println("   📡 Headers: " + exchange.getRequest().getHeaders().toSingleValueMap());
            
            // Add custom headers
            exchange.getRequest().mutate()
                    .header("X-Request-ID", requestId)
                    .header("X-Gateway-Timestamp", startTime)
                    .build();
            
            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    // Log response
                    System.out.println("✅ GATEWAY RESPONSE [" + requestId + "]");
                    System.out.println("   📊 Status: " + exchange.getResponse().getStatusCode());
                    System.out.println("   ⏱️ Duration: " + java.time.Duration.between(
                        Instant.parse(startTime), Instant.now()).toMillis() + "ms");
                    System.out.println("   📤 Response Headers: " + exchange.getResponse().getHeaders().toSingleValueMap());
                })
            );
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}