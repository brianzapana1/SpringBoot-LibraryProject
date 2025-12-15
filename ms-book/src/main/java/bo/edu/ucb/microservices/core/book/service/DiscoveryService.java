package bo.edu.ucb.microservices.core.book.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DiscoveryService {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    @Qualifier("loadBalancedWebClientBuilder")
    private WebClient.Builder loadBalancedWebClientBuilder;

    /**
     * Obtiene todas las instancias registradas en Eureka
     */
    public List<String> getAllRegisteredServices() {
        return discoveryClient.getServices();
    }

    /**
     * Obtiene información de una instancia específica por nombre del servicio
     */
    public List<ServiceInstance> getServiceInstances(String serviceName) {
        return discoveryClient.getInstances(serviceName);
    }

    /**
     * Obtiene información del propio servicio (MS-BOOK) para demostrar autodescubrimiento
     */
    public Mono<String> getSelfServiceInfo() {
        // Usar el nombre lógico del servicio en lugar de IP:puerto
        return loadBalancedWebClientBuilder
                .build()
                .get()
                .uri("http://MS-BOOK/books/count")
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Obtiene información básica de Eureka Server usando discovery
     */
    public Mono<String> getEurekaInfo() {
        // Usar el nombre lógico del servidor Eureka
        return loadBalancedWebClientBuilder
                .build()
                .get()
                .uri("http://DISCOVERY-SERVER/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Eureka server not accessible via service discovery");
    }
}