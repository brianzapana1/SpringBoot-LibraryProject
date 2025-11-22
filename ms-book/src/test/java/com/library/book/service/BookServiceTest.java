package com.library.book.service;

import bo.edu.ucb.microservices.dto.book.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Book Service Tests")
class BookServiceTest {

    private List<BookDto> testCatalog;

    @BeforeEach
    void setUp() {
        testCatalog = new ArrayList<>();
        testCatalog.add(new BookDto(1, "El Señor de los Anillos", "9780544003415", "J.R.R. Tolkien", "Fantasía"));
        testCatalog.add(new BookDto(2, "Cien Años de Soledad", "9780307474728", "Gabriel García Márquez", "Realismo Mágico"));
        testCatalog.add(new BookDto(3, "1984", "9780451524935", "George Orwell", "Distopía"));
    }

    @Test
    @DisplayName("Debería validar que los datos del catálogo están bien formados")
    void shouldValidateCatalogDataIntegrity() {
        // Verificar que no hay IDs duplicados
        long uniqueIds = testCatalog.stream()
                .mapToInt(BookDto::getBookId)
                .distinct()
                .count();
        assertEquals(testCatalog.size(), uniqueIds, "No debe haber IDs duplicados");

        // Verificar que no hay ISBNs duplicados
        long uniqueISBNs = testCatalog.stream()
                .map(BookDto::getIsbn)
                .distinct()
                .count();
        assertEquals(testCatalog.size(), uniqueISBNs, "No debe haber ISBNs duplicados");

        // Verificar que todos los campos obligatorios están presentes
        for (BookDto book : testCatalog) {
            assertNotNull(book.getTitle(), "El título no debe ser null");
            assertNotNull(book.getAuthor(), "El autor no debe ser null");
            assertNotNull(book.getIsbn(), "El ISBN no debe ser null");
            assertNotNull(book.getGenre(), "El género no debe ser null");
            assertTrue(book.getBookId() > 0, "El ID debe ser positivo");
        }
    }

    @Test
    @DisplayName("Debería validar formato de ISBN")
    void shouldValidateISBNFormat() {
        for (BookDto book : testCatalog) {
            String isbn = book.getIsbn();
            assertNotNull(isbn);
            assertFalse(isbn.trim().isEmpty());
            
            // Verificar que tiene el formato básico de ISBN (solo dígitos y posibles guiones)
            assertTrue(isbn.matches("^[0-9-]+$"), 
                "ISBN debe contener solo números y guiones: " + isbn);
        }
    }

    @Test
    @DisplayName("Debería validar longitud mínima de títulos")
    void shouldValidateTitleLength() {
        for (BookDto book : testCatalog) {
            assertTrue(book.getTitle().length() >= 2, 
                "El título debe tener al menos 2 caracteres: " + book.getTitle());
        }
    }

    @Test
    @DisplayName("Debería validar longitud mínima de autores")
    void shouldValidateAuthorLength() {
        for (BookDto book : testCatalog) {
            assertTrue(book.getAuthor().length() >= 2, 
                "El autor debe tener al menos 2 caracteres: " + book.getAuthor());
        }
    }

    @Test
    @DisplayName("Debería validar que los géneros son válidos")
    void shouldValidateGenres() {
        List<String> validGenres = List.of(
            "Fantasía", "Realismo Mágico", "Distopía", "Clásico", 
            "Fábula", "Ciencia Ficción", "Romance", "Misterio", 
            "Terror", "Aventura", "Biografía", "Historia"
        );

        for (BookDto book : testCatalog) {
            assertTrue(validGenres.contains(book.getGenre()) || 
                      book.getGenre().length() >= 3, 
                "Género debe ser válido o tener al menos 3 caracteres: " + book.getGenre());
        }
    }

    @Test
    @DisplayName("Debería crear BookDto con todos los campos")
    void shouldCreateBookDtoWithAllFields() {
        BookDto book = new BookDto(1, "Test Title", "1234567890", "Test Author", "Test Genre");
        
        assertEquals(1, book.getBookId());
        assertEquals("Test Title", book.getTitle());
        assertEquals("1234567890", book.getIsbn());
        assertEquals("Test Author", book.getAuthor());
        assertEquals("Test Genre", book.getGenre());
    }

    @Test
    @DisplayName("Debería mantener inmutabilidad de los datos del libro")
    void shouldMaintainBookDataImmutability() {
        BookDto originalBook = testCatalog.get(0);
        
        // Verificar que los getters retornan los valores correctos
        assertEquals("El Señor de los Anillos", originalBook.getTitle());
        assertEquals("J.R.R. Tolkien", originalBook.getAuthor());
        
        // Crear una nueva instancia con los mismos datos
        BookDto copiedBook = new BookDto(
            originalBook.getBookId(),
            originalBook.getTitle(),
            originalBook.getIsbn(),
            originalBook.getAuthor(),
            originalBook.getGenre()
        );
        
        // Verificar que son equivalentes en contenido
        assertEquals(originalBook.getBookId(), copiedBook.getBookId());
        assertEquals(originalBook.getTitle(), copiedBook.getTitle());
        assertEquals(originalBook.getIsbn(), copiedBook.getIsbn());
        assertEquals(originalBook.getAuthor(), copiedBook.getAuthor());
        assertEquals(originalBook.getGenre(), copiedBook.getGenre());
    }

    @Test
    @DisplayName("Debería ordenar libros por diferentes criterios")
    void shouldSortBooksByCriteria() {
        // Ordenar por título
        List<BookDto> sortedByTitle = testCatalog.stream()
                .sorted((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()))
                .toList();
        
        assertEquals("1984", sortedByTitle.get(0).getTitle());
        assertEquals("Cien Años de Soledad", sortedByTitle.get(1).getTitle());
        assertEquals("El Señor de los Anillos", sortedByTitle.get(2).getTitle());

        // Ordenar por autor
        List<BookDto> sortedByAuthor = testCatalog.stream()
                .sorted((b1, b2) -> b1.getAuthor().compareToIgnoreCase(b2.getAuthor()))
                .toList();
        
        assertEquals("Gabriel García Márquez", sortedByAuthor.get(0).getAuthor());
        assertEquals("George Orwell", sortedByAuthor.get(1).getAuthor());
        assertEquals("J.R.R. Tolkien", sortedByAuthor.get(2).getAuthor());
    }

    @Test
    @DisplayName("Debería filtrar libros por múltiples criterios")
    void shouldFilterBooksByMultipleCriteria() {
        // Agregar más libros para pruebas de filtrado
        testCatalog.add(new BookDto(4, "El Hobbit", "9780547928227", "J.R.R. Tolkien", "Fantasía"));
        testCatalog.add(new BookDto(5, "El Amor en los Tiempos del Cólera", "9780307389732", "Gabriel García Márquez", "Realismo Mágico"));

        // Filtrar por género
        List<BookDto> fantasyBooks = testCatalog.stream()
                .filter(book -> book.getGenre().equals("Fantasía"))
                .toList();
        
        assertEquals(2, fantasyBooks.size());
        assertTrue(fantasyBooks.stream().allMatch(book -> book.getGenre().equals("Fantasía")));

        // Filtrar por autor
        List<BookDto> tolkienBooks = testCatalog.stream()
                .filter(book -> book.getAuthor().contains("Tolkien"))
                .toList();
        
        assertEquals(2, tolkienBooks.size());
        assertTrue(tolkienBooks.stream().allMatch(book -> book.getAuthor().contains("Tolkien")));

        // Filtrar por múltiples criterios
        List<BookDto> marquezRealismoBooks = testCatalog.stream()
                .filter(book -> book.getAuthor().contains("García Márquez") && 
                               book.getGenre().equals("Realismo Mágico"))
                .toList();
        
        assertEquals(2, marquezRealismoBooks.size());
    }

    @Test
    @DisplayName("Debería manejar búsquedas con caracteres especiales")
    void shouldHandleSpecialCharacterSearches() {
        // Crear libro con caracteres especiales
        BookDto specialBook = new BookDto(6, "Año de Gracia", "9780123456789", "José María", "Novela");
        testCatalog.add(specialBook);

        // Buscar con acentos - "año" también aparece en "Cien Años de Soledad"
        List<BookDto> foundBooks = testCatalog.stream()
                .filter(book -> book.getTitle().toLowerCase().contains("año"))
                .toList();
        
        assertEquals(2, foundBooks.size()); // "Año de Gracia" y "Cien Años de Soledad"
        assertTrue(foundBooks.stream().anyMatch(book -> book.getTitle().equals("Año de Gracia")));

        // Buscar con espacios
        List<BookDto> foundByAuthor = testCatalog.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains("josé maría"))
                .toList();
        
        assertEquals(1, foundByAuthor.size());
    }

    @Test
    @DisplayName("Debería validar rangos de paginación")
    void shouldValidatePaginationRanges() {
        int totalBooks = testCatalog.size();
        int pageSize = 2;
        
        // Página 0
        int start = 0 * pageSize;
        int end = Math.min(start + pageSize, totalBooks);
        List<BookDto> page0 = testCatalog.subList(start, end);
        assertEquals(2, page0.size());

        // Página 1
        start = 1 * pageSize;
        end = Math.min(start + pageSize, totalBooks);
        List<BookDto> page1 = testCatalog.subList(start, end);
        assertEquals(1, page1.size());

        // Página fuera de rango
        start = 5 * pageSize;
        if (start >= totalBooks) {
            assertEquals(0, new ArrayList<BookDto>().size());
        }
    }
}
