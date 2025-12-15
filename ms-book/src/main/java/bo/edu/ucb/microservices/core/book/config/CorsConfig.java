package bo.edu.ucb.microservices.core.book.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS configuration DISABLED for ms-book.
 * 
 * IMPORTANT: CORS is handled exclusively by the Gateway to prevent
 * duplicate CORS headers when requests flow through Gateway -> ms-book.
 * 
 * When both services add CORS headers, browsers receive invalid duplicates like:
 * "Access-Control-Allow-Origin: http://localhost:4200 http://localhost:4200"
 * which causes the browser to reject the response entirely.
 * 
 * The Gateway's CorsGlobalConfig handles all CORS for the entire system.
 */
@Configuration
public class CorsConfig {
    // CORS disabled - handled by Gateway
}
