package bo.edu.ucb.microservices.core.book.controller;

import bo.edu.ucb.microservices.core.book.entity.Book;
import bo.edu.ucb.microservices.core.book.service.BookService;
import bo.edu.ucb.microservices.util.http.ServiceUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Tag(name = "Book Catalog", description = "API para gestión del catálogo de libros")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/ms-book/v1/api")
public class BookController {

    private static final Logger LOG = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public BookController(BookService bookService, ServiceUtil serviceUtil) {
        this.bookService = bookService;
        this.serviceUtil = serviceUtil;
    }

    /**
     * Obtener todo el catálogo de libros con paginación
     */
    @Operation(summary = "Obtener catálogo completo", 
               description = "Retorna todos los libros disponibles en el catálogo con soporte de paginación")
    @GetMapping(value = "/books", produces = "application/json")
    //@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<Book> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        LOG.info("Obteniendo catálogo de libros - página: {}, tamaño: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.getAllBooks(pageable);
        
        LOG.info("Catálogo obtenido: {} libros en página {}", books.getNumberOfElements(), page);
        return books;
    }

    /**
     * Obtener todos los libros sin paginación
     */
    @GetMapping(value = "/books/all", produces = "application/json")
    //@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Book> getAllBooksNoPagination() {
        LOG.info("Obteniendo todos los libros desde {}", serviceUtil.getServiceAddress());
        List<Book> books = bookService.getAllBooks();
        return books;
    }

    /**
     * Buscar libro por ID
     */
    @Operation(summary = "Buscar libro por ID", 
               description = "Retorna un libro específico basado en su ID")
    @GetMapping(value = "/books/{bookId}", produces = "application/json")
    //@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Book> getBookById(@PathVariable Long bookId) {
        
        LOG.info("Buscando libro con ID: {}", bookId);
        
        Optional<Book> book = bookService.getBookById(bookId);
        
        if (book.isPresent()) {
            LOG.info("Libro encontrado: {}", book.get().getTitle());
            return ResponseEntity.ok(book.get());
        } else {
            LOG.warn("Libro con ID {} no encontrado", bookId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crear un nuevo libro
     */
    @Operation(summary = "Crear nuevo libro", 
               description = "Añade un nuevo libro al catálogo")
    @PostMapping(value = "/books", produces = "application/json")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        
        LOG.info("Creando nuevo libro: {}", book.getTitle());
        
        try {
            Book createdBook = bookService.saveBook(book);
            LOG.info("Libro creado exitosamente con ID: {}", createdBook.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (Exception e) {
            LOG.error("Error al crear libro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Actualizar un libro existente
     */
    @Operation(summary = "Actualizar libro", 
               description = "Actualiza la información de un libro existente")
    @PutMapping(value = "/books/{bookId}", produces = "application/json")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> updateBook(@PathVariable Long bookId, @Valid @RequestBody Book book) {
        
        LOG.info("Actualizando libro con ID: {}", bookId);
        
        try {
            Book updatedBook = bookService.updateBook(bookId, book);
            LOG.info("Libro actualizado exitosamente: {}", updatedBook.getTitle());
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            LOG.error("Error al actualizar libro: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Eliminar un libro
     */
    @Operation(summary = "Eliminar libro", 
               description = "Elimina un libro del catálogo")
    @DeleteMapping(value = "/books/{bookId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        
        LOG.info("Eliminando libro con ID: {}", bookId);
        
        try {
            bookService.deleteBook(bookId);
            LOG.info("Libro eliminado exitosamente");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            LOG.error("Error al eliminar libro: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Buscar libros por título
     */
    @Operation(summary = "Buscar por título", 
               description = "Busca libros que contengan el título especificado")
    @GetMapping(value = "/search/title", produces = "application/json")
    public List<Book> findByTitle(@RequestParam String query) {
        
        LOG.info("Buscando libros por título: {}", query);
        
        List<Book> books = bookService.getBooksByTitle(query);
        
        LOG.info("Encontrados {} libros que contienen '{}'", books.size(), query);
        return books;
    }

    /**
     * Buscar libros por autor
     */
    @Operation(summary = "Buscar por autor", 
               description = "Busca libros del autor especificado")
    @GetMapping(value = "/search/author", produces = "application/json")
    public List<Book> findByAuthor(@RequestParam String query) {
        
        LOG.info("Buscando libros por autor: {}", query);
        
        List<Book> books = bookService.getBooksByAuthor(query);
        
        LOG.info("Encontrados {} libros del autor '{}'", books.size(), query);
        return books;
    }

    /**
     * Buscar libros por género
     */
    @Operation(summary = "Buscar por género", 
               description = "Busca libros de la categoría especificada")
    @GetMapping(value = "/search/genre", produces = "application/json")
    public List<Book> findByGenre(@RequestParam String genre) {
        
        LOG.info("Buscando libros por género: {}", genre);
        
        List<Book> books = bookService.getBooksByGenre(genre);
        
        LOG.info("Encontrados {} libros del género '{}'", books.size(), genre);
        return books;
    }

    /**
     * Buscar libros por ISBN
     */
    @Operation(summary = "Buscar por ISBN", 
               description = "Busca un libro por su código ISBN")
    @GetMapping(value = "/search/isbn", produces = "application/json")
    public ResponseEntity<Book> findByIsbn(@RequestParam String isbn) {
        
        LOG.info("Buscando libro por ISBN: {}", isbn);
        
        Optional<Book> book = bookService.getBookByIsbn(isbn);
        
        if (book.isPresent()) {
            LOG.info("Libro encontrado por ISBN: {}", book.get().getTitle());
            return ResponseEntity.ok(book.get());
        } else {
            LOG.warn("No se encontró libro con ISBN: {}", isbn);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener libros disponibles
     */
    @GetMapping(value = "/available", produces = "application/json")
    public List<Book> getAvailableBooks() {
        LOG.info("Obteniendo libros disponibles desde {}", serviceUtil.getServiceAddress());
        List<Book> books = bookService.getAvailableBooks();
        return books;
    }

    /**
     * Obtener información del usuario autenticado a través del token JWT
     */
    @Operation(summary = "Información del usuario", 
               description = "Retorna información básica del usuario autenticado y los claims del token")
    @GetMapping(value = "/user/info", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getAuthenticatedUserInfo(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            LOG.warn("Solicitud de user info sin autenticación válida");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Usuario no autenticado"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authenticated", authentication.isAuthenticated());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        response.put("roles", roles);

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            response.put("tokenAttributes", jwtAuth.getTokenAttributes());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Prestar libro
     */
    @PostMapping("/books/{id}/borrow")
    @Operation(summary = "Prestar libro", description = "Registra el préstamo de un libro")
    public ResponseEntity<Map<String, String>> borrowBook(@PathVariable Long id) {
        LOG.info("Prestando libro ID: {} desde {}", id, serviceUtil.getServiceAddress());
        boolean success = bookService.borrowBook(id);
        
        Map<String, String> response = Map.of(
            "message", success ? "Libro prestado exitosamente" : "No se pudo prestar el libro",
            "serviceAddress", serviceUtil.getServiceAddress()
        );
        
        return success ? ResponseEntity.ok(response) 
                      : ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Devolver libro
     */
    @PostMapping("/books/{id}/return")
    @Operation(summary = "Devolver libro", description = "Registra la devolución de un libro")
    public ResponseEntity<Map<String, String>> returnBook(@PathVariable Long id) {
        LOG.info("Devolviendo libro ID: {} desde {}", id, serviceUtil.getServiceAddress());
        boolean success = bookService.returnBook(id);
        
        Map<String, String> response = Map.of(
            "message", success ? "Libro devuelto exitosamente" : "No se pudo devolver el libro",
            "serviceAddress", serviceUtil.getServiceAddress()
        );
        
        return success ? ResponseEntity.ok(response) 
                      : ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Health check
     */
    @Operation(security = {})
    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<Map<String, String>> health() {
        LOG.debug("Health check desde {}", serviceUtil.getServiceAddress());
        Map<String, String> response = Map.of(
            "status", "UP",
            "service", "ms-book",
            "serviceAddress", serviceUtil.getServiceAddress()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Service info endpoint
     */
    @GetMapping("/info")
    public String info() {
        return "Microservicio de Books - Versión: 1.0.0\nAddress: " + serviceUtil.getServiceAddress();
    }
    
    // ========== ENDPOINTS QUE DEMUESTRAN DIFERENTES TIPOS DE CONSULTAS ==========
    
    /**
     * NATIVE QUERY EXAMPLE - Obtener estadísticas por género
     */
    @Operation(summary = "Estadísticas por género", 
               description = "Obtiene estadísticas de libros agrupadas por género usando consulta SQL nativa")
    @GetMapping(value = "/statistics/genre", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getBookStatisticsByGenre() {
        LOG.info("Obteniendo estadísticas por género (Native Query)");
        
        List<Object[]> rawData = bookService.getBookStatisticsByGenre();
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        for (Object[] row : rawData) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("genre", row[0]);
            stat.put("totalBooks", row[1]);
            stat.put("availableCopies", row[2]);
            statistics.add(stat);
        }
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * NATIVE QUERY EXAMPLE - Libros más prestados
     */
    @Operation(summary = "Libros más prestados", 
               description = "Obtiene los libros más prestados usando consulta SQL nativa")
    @GetMapping(value = "/popular", produces = "application/json")
    public List<Book> getMostBorrowedBooks(@RequestParam(defaultValue = "5") int limit) {
        LOG.info("Obteniendo {} libros más prestados (Native Query)", limit);
        return bookService.getMostBorrowedBooks(limit);
    }
    
    /**
     * NATIVE QUERY EXAMPLE - Búsqueda de texto completo
     */
    @Operation(summary = "Búsqueda de texto completo", 
               description = "Busca en título, autor y descripción usando consulta SQL nativa")
    @GetMapping(value = "/search/fulltext", produces = "application/json")
    public List<Book> searchBooksFullText(@RequestParam String query) {
        LOG.info("Búsqueda de texto completo: {} (Native Query)", query);
        return bookService.searchBooksFullText(query);
    }
    
    /**
     * DERIVED QUERY EXAMPLE - Libros por rango de páginas
     */
    @Operation(summary = "Buscar por rango de páginas", 
               description = "Busca libros entre un rango específico de páginas usando Derived Query")
    @GetMapping(value = "/search/pages", produces = "application/json")
    public List<Book> getBooksByPageRange(
            @RequestParam Integer minPages, 
            @RequestParam Integer maxPages) {
        
        LOG.info("Buscando libros con páginas entre {} y {} (Derived Query)", minPages, maxPages);
        return bookService.getBooksByPageRange(minPages, maxPages);
    }
    
    /**
     * JPQL QUERY EXAMPLE - Autores únicos
     */
    @Operation(summary = "Obtener autores únicos", 
               description = "Obtiene lista de autores únicos usando consulta JPQL")
    @GetMapping(value = "/authors", produces = "application/json")
    public List<String> getDistinctAuthors() {
        LOG.info("Obteniendo autores únicos (JPQL Query)");
        return bookService.getDistinctAuthors();
    }
    
    /**
     * CRITERIA QUERY EXAMPLE - Búsqueda avanzada
     */
    @Operation(summary = "Búsqueda avanzada", 
               description = "Búsqueda con múltiples criterios usando Criteria API")
    @GetMapping(value = "/search/advanced", produces = "application/json")
    public List<Book> searchBooksAdvanced(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages) {
        
        LOG.info("Búsqueda avanzada con Criteria API");
        
        LocalDate from = (fromDate != null) ? LocalDate.parse(fromDate) : null;
        LocalDate to = (toDate != null) ? LocalDate.parse(toDate) : null;
        
        return bookService.searchBooksWithCriteria(title, author, genre, from, to, minPages, maxPages);
    }
    
    /**
     * CRITERIA QUERY EXAMPLE - Búsqueda avanzada con paginación
     */
    @Operation(summary = "Búsqueda avanzada paginada", 
               description = "Búsqueda con paginación usando Criteria API")
    @GetMapping(value = "/search/advanced/paginated", produces = "application/json")
    public Page<Book> searchBooksAdvancedPaginated(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        LOG.info("Búsqueda avanzada paginada con Criteria API");
        
        Pageable pageable = PageRequest.of(page, size, 
            "DESC".equalsIgnoreCase(sortDirection) ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending());
        
        return bookService.searchBooksAdvanced(searchTerm, genre, available, pageable);
    }
}