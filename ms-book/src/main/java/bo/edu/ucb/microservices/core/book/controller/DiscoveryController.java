package bo.edu.ucb.microservices.core.book.controller;

import bo.edu.ucb.microservices.core.book.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/discovery")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    /**
     * Obtiene todos los servicios registrados en Eureka
     * GET /discovery/services
     */
    @GetMapping("/services")
    public Map<String, Object> getAllServices() {
        Map<String, Object> response = new HashMap<>();
        List<String> services = discoveryService.getAllRegisteredServices();
        
        response.put("registered_services", services);
        response.put("total_count", services.size());
        response.put("message", "Servicios obtenidos mediante descubrimiento dinámico con Eureka");
        
        return response;
    }

    /**
     * Obtiene información de instancias de un servicio específico
     * GET /discovery/services/{serviceName}/instances
     */
    @GetMapping("/services/{serviceName}/instances")
    public Map<String, Object> getServiceInstances(@PathVariable String serviceName) {
        Map<String, Object> response = new HashMap<>();
        List<ServiceInstance> instances = discoveryService.getServiceInstances(serviceName.toUpperCase());
        
        response.put("service_name", serviceName.toUpperCase());
        response.put("instances", instances);
        response.put("instance_count", instances.size());
        response.put("message", "Instancias obtenidas mediante Eureka Discovery");
        
        return response;
    }

    /**
     * Test de comunicación usando nombre lógico del servicio
     * GET /discovery/test/self-communication
     */
    @GetMapping("/test/self-communication")
    public Mono<Map<String, Object>> testSelfCommunication() {
        return discoveryService.getSelfServiceInfo()
                .map(result -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("test_type", "self_communication_via_eureka");
                    response.put("service_name", "MS-BOOK");
                    response.put("method", "Comunicación usando nombre lógico en lugar de IP:puerto");
                    response.put("endpoint_called", "http://MS-BOOK/books/count");
                    response.put("result", result);
                    response.put("success", true);
                    return response;
                })
                .onErrorReturn(Map.of(
                        "test_type", "self_communication_via_eureka",
                        "error", "Error en comunicación via service discovery",
                        "success", false
                ));
    }

    /**
     * Test de comunicación con Eureka Server usando discovery
     * GET /discovery/test/eureka-communication
     */
    @GetMapping("/test/eureka-communication")
    public Mono<Map<String, Object>> testEurekaCommunication() {
        return discoveryService.getEurekaInfo()
                .map(result -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("test_type", "eureka_server_communication");
                    response.put("service_name", "DISCOVERY-SERVER");
                    response.put("method", "Comunicación con Eureka Server usando nombre lógico");
                    response.put("endpoint_called", "http://DISCOVERY-SERVER/actuator/health");
                    response.put("result", result);
                    response.put("success", true);
                    return response;
                })
                .onErrorReturn(Map.of(
                        "test_type", "eureka_server_communication",
                        "error", "Error en comunicación con Eureka Server",
                        "success", false
                ));
    }

    /**
     * Test completo de descubrimiento de servicios
     * GET /discovery/test/complete
     */
    @GetMapping("/test/complete")
    public Map<String, Object> completeDiscoveryTest() {
        Map<String, Object> response = new HashMap<>();
        
        // Obtener todos los servicios
        List<String> services = discoveryService.getAllRegisteredServices();
        
        // Obtener instancias de cada servicio
        Map<String, Object> servicesInfo = new HashMap<>();
        for (String serviceName : services) {
            List<ServiceInstance> instances = discoveryService.getServiceInstances(serviceName);
            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("instance_count", instances.size());
            serviceInfo.put("instances", instances.stream().map(instance -> Map.of(
                "instance_id", instance.getInstanceId(),
                "host", instance.getHost(),
                "port", instance.getPort(),
                "uri", instance.getUri().toString(),
                "secure", instance.isSecure(),
                "metadata", instance.getMetadata()
            )).toList());
            servicesInfo.put(serviceName, serviceInfo);
        }
        
        response.put("test_type", "complete_service_discovery");
        response.put("message", "Descubrimiento completo de servicios registrados en Eureka");
        response.put("total_services", services.size());
        response.put("services_detail", servicesInfo);
        response.put("discovery_method", "Netflix Eureka Service Registry");
        response.put("success", true);
        
        return response;
    }
}