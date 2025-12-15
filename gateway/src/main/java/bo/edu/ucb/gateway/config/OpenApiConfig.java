package bo.edu.ucb.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String gatewayPort;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
            .info(new Info()
                .title("Library Management System - API Gateway")
                .description("Documentación completa de APIs del Sistema de Gestión de Biblioteca")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Library System Team")
                    .email("dev@library.com")
                    .url("https://github.com/library-system"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(Arrays.asList(
                new Server().url("http://localhost:" + gatewayPort).description("API Gateway - Desarrollo"),
                new Server().url("http://localhost:8082").description("MS-Book - Directo (Solo para desarrollo)")
            ))
            .components(new Components().addSecuritySchemes("bearer-jwt", bearerScheme()))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        
        // Agregar paths de Books directamente
        addBooksPath(openAPI);
        return openAPI;
    }

    private SecurityScheme bearerScheme() {
        // JWT Bearer configuration so Swagger UI renders the Authorize button
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Autenticación Bearer JWT emitida por Keycloak");
    }

    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
            .group("1-gateway")
            .pathsToMatch("/actuator/**", "/gateway/**", "/fallback/**")
            .build();
    }

    @Bean
    public GroupedOpenApi bookApi() {
        return GroupedOpenApi.builder()
            .group("2-books")
            .pathsToMatch("/api/books/**")
            .build();
    }

    private void addBooksPath(OpenAPI openAPI) {
        // GET /api/books (listar con paginación)
        Operation getBooks = new Operation()
            .summary("Listar libros (paginado)")
            .description("Retorna lista paginada de libros")
            .addTagsItem("Books - Library Management")
            .operationId("listBooks")
            .addParametersItem(new Parameter().name("page").in("query").description("Página").example(0).schema(new Schema<>().type("integer")))
            .addParametersItem(new Parameter().name("size").in("query").description("Tamaño").example(10).schema(new Schema<>().type("integer")))
            .responses(new ApiResponses()
                .addApiResponse("200", new ApiResponse()
                    .description("Lista obtenida")
                    .content(new Content().addMediaType("application/json", new MediaType().example("{\"content\":[{\"id\":1,\"title\":\"El Señor de los Anillos\",\"author\":\"J.R.R. Tolkien\",\"isbn\":\"9780544003415\",\"publicationDate\":\"1954-07-29\",\"pageCount\":1178,\"genre\":\"Fantasía\",\"description\":\"Épica aventura\",\"availableCopies\":3,\"totalCopies\":5}],\"pageable\":{\"pageNumber\":0,\"pageSize\":10},\"totalElements\":10}")))));

        // POST /api/books (crear libro)
        Operation postBook = new Operation()
            .summary("Crear libro")
            .description("Registra nuevo libro en el sistema")
            .addTagsItem("Books - Library Management")
            .operationId("createBook")
            .requestBody(new RequestBody()
                .required(true)
                .content(new Content().addMediaType("application/json", new MediaType().example("{\"title\":\"Cien años de soledad\",\"author\":\"Gabriel García Márquez\",\"isbn\":\"9780307474728\",\"publicationDate\":\"1967-05-30\",\"pageCount\":417,\"genre\":\"Realismo mágico\",\"description\":\"Historia Buendía\",\"availableCopies\":5,\"totalCopies\":5}"))))
            .responses(new ApiResponses()
                .addApiResponse("201", new ApiResponse()
                    .description("Libro creado")
                    .content(new Content().addMediaType("application/json", new MediaType().example("{\"id\":15,\"title\":\"Cien años de soledad\",\"author\":\"Gabriel García Márquez\",\"isbn\":\"9780307474728\",\"publicationDate\":\"1967-05-30\",\"pageCount\":417,\"genre\":\"Realismo mágico\",\"description\":\"Historia Buendía\",\"availableCopies\":5,\"totalCopies\":5}"))))
                .addApiResponse("400", new ApiResponse().description("Datos inválidos"))
                .addApiResponse("409", new ApiResponse().description("ISBN duplicado")));

        // GET /api/books/{id} (obtener por ID)
        Operation getBookById = new Operation()
            .summary("Obtener libro por ID")
            .description("Retorna libro específico")
            .addTagsItem("Books - Library Management")
            .operationId("getBookById")
            .addParametersItem(new Parameter().name("id").in("path").required(true).description("ID del libro").example(1).schema(new Schema<>().type("integer").format("int64")))
            .responses(new ApiResponses()
                .addApiResponse("200", new ApiResponse()
                    .description("Libro encontrado")
                    .content(new Content().addMediaType("application/json", new MediaType().example("{\"id\":1,\"title\":\"El Señor de los Anillos - Edición Actualizada\",\"author\":\"J.R.R. Tolkien\",\"isbn\":\"9780544003415\",\"publicationDate\":\"1954-07-29\",\"pageCount\":1220,\"genre\":\"Fantasía\",\"description\":\"Épica aventura ACTUALIZADA\",\"availableCopies\":4,\"totalCopies\":5}"))))
                .addApiResponse("404", new ApiResponse().description("Libro no encontrado")));

        // PUT /api/books/{id} (actualizar)
        Operation putBook = new Operation()
            .summary("Actualizar libro")
            .description("Modifica datos de libro existente")
            .addTagsItem("Books - Library Management")
            .operationId("updateBook")
            .addParametersItem(new Parameter().name("id").in("path").required(true).description("ID").example(1).schema(new Schema<>().type("integer").format("int64")))
            .requestBody(new RequestBody()
                .required(true)
                .content(new Content().addMediaType("application/json", new MediaType().example("{\"title\":\"El Señor de los Anillos - Nueva Edición\",\"author\":\"J.R.R. Tolkien\",\"isbn\":\"9780544003415\",\"publicationDate\":\"1954-07-29\",\"pageCount\":1250,\"genre\":\"Fantasía Épica\",\"description\":\"Edición actualizada\",\"availableCopies\":6,\"totalCopies\":8}"))))
            .responses(new ApiResponses()
                .addApiResponse("200", new ApiResponse()
                    .description("Libro actualizado")
                    .content(new Content().addMediaType("application/json", new MediaType().example("{\"id\":1,\"title\":\"El Señor de los Anillos - Nueva Edición\",\"author\":\"J.R.R. Tolkien\",\"isbn\":\"9780544003415\",\"publicationDate\":\"1954-07-29\",\"pageCount\":1250,\"genre\":\"Fantasía Épica\",\"description\":\"Edición actualizada\",\"availableCopies\":6,\"totalCopies\":8}"))))
                .addApiResponse("404", new ApiResponse().description("No encontrado"))
                .addApiResponse("400", new ApiResponse().description("Datos inválidos")));

        // DELETE /api/books/{id} (eliminar)
        Operation deleteBook = new Operation()
            .summary("Eliminar libro")
            .description("Elimina libro permanentemente")
            .addTagsItem("Books - Library Management")
            .operationId("deleteBook")
            .addParametersItem(new Parameter().name("id").in("path").required(true).description("ID a eliminar").example(1).schema(new Schema<>().type("integer").format("int64")))
            .responses(new ApiResponses()
                .addApiResponse("204", new ApiResponse().description("Libro eliminado"))
                .addApiResponse("404", new ApiResponse().description("No encontrado")));

        // Crear PathItems
        PathItem booksPath = new PathItem().get(getBooks).post(postBook);
        PathItem booksIdPath = new PathItem().get(getBookById).put(putBook).delete(deleteBook);

        // Agregar a OpenAPI
        openAPI.path("/api/books", booksPath);
        openAPI.path("/api/books/{id}", booksIdPath);
    }
}
