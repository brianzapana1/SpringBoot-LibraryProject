package bo.edu.ucb.microservices.core.book.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import bo.edu.ucb.microservices.dto.book.BookDto;
import bo.edu.ucb.microservices.util.exceptions.InvalidInputException;
import bo.edu.ucb.microservices.util.exceptions.NotFoundException;
import bo.edu.ucb.microservices.util.http.ServiceUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Catálogo de Libros", description = "API para búsqueda y consulta del catálogo de libros")
@Validated
@RequestMapping("/v1/catalog")
public class BookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);
    private final ServiceUtil serviceUtil;
    
    // Simulación de datos del catálogo para demostración (mutable para permitir adiciones)
    private final List<BookDto> catalogData;

    @Autowired
    public BookController(ServiceUtil serviceUtil){
        this.serviceUtil = serviceUtil;
        this.catalogData = initializeCatalog();
    }

    /**
     * Inicializa el catálogo con datos de ejemplo
     */
    private List<BookDto> initializeCatalog() {
        List<BookDto> books = new ArrayList<>();
        books.add(new BookDto(1, "El Señor de los Anillos", "9780544003415", "J.R.R. Tolkien", "Fantasía"));
        books.add(new BookDto(2, "Cien Años de Soledad", "9780307474728", "Gabriel García Márquez", "Realismo Mágico"));
        books.add(new BookDto(3, "1984", "9780451524935", "George Orwell", "Distopía"));
        books.add(new BookDto(4, "Don Quijote de la Mancha", "9788491050940", "Miguel de Cervantes", "Clásico"));
        books.add(new BookDto(5, "Harry Potter y la Piedra Filosofal", "9788498383440", "J.K. Rowling", "Fantasía"));
        books.add(new BookDto(6, "El Principito", "9780156012195", "Antoine de Saint-Exupéry", "Fábula"));
        books.add(new BookDto(7, "Crónica de una Muerte Anunciada", "9780307388285", "Gabriel García Márquez", "Realismo Mágico"));
        books.add(new BookDto(8, "El Hobbit", "9780547928227", "J.R.R. Tolkien", "Fantasía"));
        return books;
    }

    /**
     * Obtener todo el catálogo de libros con paginación
     */
    @Operation(summary = "Obtener catálogo completo", 
               description = "Retorna todos los libros disponibles en el catálogo con soporte de paginación")
    @GetMapping(value = "/books", produces = "application/json")
    public List<BookDto> getAllBooks(
            @Parameter(description = "Página a obtener (empezando desde 0)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Número de libros por página", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        LOGGER.info("Obteniendo catálogo completo - Página: {}, Tamaño: {}", page, size);
        
        int start = page * size;
        int end = Math.min(start + size, catalogData.size());
        
        if (start >= catalogData.size()) {
            return new ArrayList<>();
        }
        
        return catalogData.subList(start, end);
    }

    /**
     * Buscar libros por título
     */
    @Operation(summary = "Buscar por título", 
               description = "Busca libros que contengan el término especificado en el título")
    @GetMapping(value = "/search/title", produces = "application/json")
    public List<BookDto> searchByTitle(
            @Parameter(description = "Término de búsqueda en el título", example = "señor")
            @RequestParam String query) {
        
        LOGGER.info("Buscando libros por título: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidInputException("El término de búsqueda no puede estar vacío");
        }
        
        return catalogData.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Buscar libros por autor
     */
    @Operation(summary = "Buscar por autor", 
               description = "Busca libros que contengan el término especificado en el nombre del autor")
    @GetMapping(value = "/search/author", produces = "application/json")
    public List<BookDto> searchByAuthor(
            @Parameter(description = "Término de búsqueda en el autor", example = "tolkien")
            @RequestParam String query) {
        
        LOGGER.info("Buscando libros por autor: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidInputException("El término de búsqueda no puede estar vacío");
        }
        
        return catalogData.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Buscar libros por género
     */
    @Operation(summary = "Buscar por género", 
               description = "Busca libros de un género específico")
    @GetMapping(value = "/search/genre", produces = "application/json")
    public List<BookDto> searchByGenre(
            @Parameter(description = "Género de libro", example = "Fantasía")
            @RequestParam String genre) {
        
        LOGGER.info("Buscando libros por género: {}", genre);
        
        if (genre == null || genre.trim().isEmpty()) {
            throw new InvalidInputException("El género no puede estar vacío");
        }
        
        return catalogData.stream()
                .filter(book -> book.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Buscar libro por ISBN exacto
     */
    @Operation(summary = "Buscar por ISBN", 
               description = "Busca un libro específico por su ISBN")
    @GetMapping(value = "/search/isbn", produces = "application/json")
    public BookDto searchByISBN(
            @Parameter(description = "ISBN del libro", example = "9780544003415")
            @RequestParam String isbn) {
        
        LOGGER.info("Buscando libro por ISBN: {}", isbn);
        
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new InvalidInputException("El ISBN no puede estar vacío");
        }
        
        return catalogData.stream()
                .filter(book -> book.getIsbn().equals(isbn))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No se encontró libro con ISBN: " + isbn));
    }

    /**
     * Obtener detalles de un libro específico por ID
     */
    @Operation(summary = "Obtener detalles de libro", 
               description = "Obtiene los detalles completos de un libro por su ID")
    @GetMapping(value = "/books/{bookId}", produces = "application/json")
    public BookDto getBookDetails(
            @Parameter(description = "ID del libro", example = "1")
            @PathVariable("bookId") @Min(1) int bookId) {
        
        LOGGER.info("Obteniendo detalles del libro con ID: {}", bookId);
        
        return catalogData.stream()
                .filter(book -> book.getBookId() == bookId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No se encontró libro con ID: " + bookId));
    }

    /**
     * Búsqueda general en todo el catálogo
     */
    @Operation(summary = "Búsqueda general", 
               description = "Busca el término en título, autor y género simultáneamente")
    @GetMapping(value = "/search", produces = "application/json")
    public List<BookDto> generalSearch(
            @Parameter(description = "Término de búsqueda general", example = "fantasía")
            @RequestParam String query) {
        
        LOGGER.info("Realizando búsqueda general: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidInputException("El término de búsqueda no puede estar vacío");
        }
        
        String lowerQuery = query.toLowerCase();
        return catalogData.stream()
                .filter(book -> 
                    book.getTitle().toLowerCase().contains(lowerQuery) ||
                    book.getAuthor().toLowerCase().contains(lowerQuery) ||
                    book.getGenre().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Crear un nuevo libro en el catálogo
     */
    @Operation(summary = "Crear nuevo libro", 
               description = "Añade un nuevo libro al catálogo de la biblioteca")
    @PostMapping(value = "/books", produces = "application/json")
    public ResponseEntity<BookDto> createBook(
            @Parameter(description = "Datos del libro a crear")
            @Valid @RequestBody BookDto bookRequest) {
        
        LOGGER.info("Creando nuevo libro: {}", bookRequest.getTitle());
        
        // Validaciones básicas
        if (bookRequest.getTitle() == null || bookRequest.getTitle().trim().isEmpty()) {
            throw new InvalidInputException("El título del libro no puede estar vacío");
        }
        if (bookRequest.getAuthor() == null || bookRequest.getAuthor().trim().isEmpty()) {
            throw new InvalidInputException("El autor del libro no puede estar vacío");
        }
        if (bookRequest.getIsbn() == null || bookRequest.getIsbn().trim().isEmpty()) {
            throw new InvalidInputException("El ISBN del libro no puede estar vacío");
        }
        if (bookRequest.getGenre() == null || bookRequest.getGenre().trim().isEmpty()) {
            throw new InvalidInputException("El género del libro no puede estar vacío");
        }
        
        // Verificar que el ISBN no exista ya
        boolean isbnExists = catalogData.stream()
                .anyMatch(book -> book.getIsbn().equals(bookRequest.getIsbn()));
        if (isbnExists) {
            throw new InvalidInputException("Ya existe un libro con el ISBN: " + bookRequest.getIsbn());
        }
        
        // Generar nuevo ID (buscar el máximo actual + 1)
        int newId = catalogData.stream()
                .mapToInt(BookDto::getBookId)
                .max()
                .orElse(0) + 1;
        
        // Crear el nuevo libro
        BookDto newBook = new BookDto(
            newId,
            bookRequest.getTitle().trim(),
            bookRequest.getIsbn().trim(),
            bookRequest.getAuthor().trim(),
            bookRequest.getGenre().trim()
        );
        
        // Añadir al catálogo
        catalogData.add(newBook);
        
        LOGGER.info("Libro creado exitosamente con ID: {}", newId);
        
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }

    /**
     * Obtener géneros disponibles en el catálogo
     */
    @Operation(summary = "Obtener géneros", 
               description = "Retorna la lista de géneros únicos disponibles en el catálogo")
    @GetMapping(value = "/genres", produces = "application/json")
    public List<String> getAvailableGenres() {
        LOGGER.info("Obteniendo géneros disponibles");
        
        return catalogData.stream()
                .map(BookDto::getGenre)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}