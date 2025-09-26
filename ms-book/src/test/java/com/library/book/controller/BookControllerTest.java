package com.library.book.controller;

import com.library.BookDto;
import com.library.util.exceptions.InvalidInputException;
import com.library.util.exceptions.NotFoundException;
import com.library.util.http.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Unit Tests")
class BookControllerTest {

    @Mock
    private ServiceUtil serviceUtil;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        // Los datos se inicializan automáticamente en el constructor del controlador
    }

    @Test
    @DisplayName("Debería obtener el catálogo completo con paginación por defecto")
    void shouldGetAllBooksWithDefaultPagination() {
        // When
        List<BookDto> result = bookController.getAllBooks(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(8, result.size()); // Todos los libros del catálogo
        assertEquals("El Señor de los Anillos", result.get(0).getTitle());
        assertEquals("El Hobbit", result.get(7).getTitle());
    }

    @Test
    @DisplayName("Debería obtener libros con paginación personalizada")
    void shouldGetBooksWithCustomPagination() {
        // When
        List<BookDto> result = bookController.getAllBooks(0, 3);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("El Señor de los Anillos", result.get(0).getTitle());
        assertEquals("1984", result.get(2).getTitle());
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando la página excede el número de libros")
    void shouldReturnEmptyListWhenPageExceedsBooks() {
        // When
        List<BookDto> result = bookController.getAllBooks(10, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debería obtener segunda página correctamente")
    void shouldGetSecondPageCorrectly() {
        // When
        List<BookDto> result = bookController.getAllBooks(1, 3);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Don Quijote de la Mancha", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Debería buscar libros por título correctamente")
    void shouldSearchBooksByTitle() {
        // When
        List<BookDto> result = bookController.searchByTitle("señor");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("El Señor de los Anillos", result.get(0).getTitle());
        assertEquals("J.R.R. Tolkien", result.get(0).getAuthor());
    }

    @Test
    @DisplayName("Debería buscar libros por título sin distinguir mayúsculas/minúsculas")
    void shouldSearchBooksByTitleCaseInsensitive() {
        // When
        List<BookDto> result = bookController.searchByTitle("HARRY");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Harry Potter y la Piedra Filosofal", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el término de búsqueda por título está vacío")
    void shouldThrowExceptionWhenTitleSearchQueryIsEmpty() {
        // When/Then
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByTitle(""));
        
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByTitle("   "));
        
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByTitle(null));
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no encuentra libros por título")
    void shouldReturnEmptyListWhenNoBookFoundByTitle() {
        // When
        List<BookDto> result = bookController.searchByTitle("inexistente");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debería buscar libros por autor correctamente")
    void shouldSearchBooksByAuthor() {
        // When
        List<BookDto> result = bookController.searchByAuthor("tolkien");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(book -> book.getTitle().equals("El Señor de los Anillos")));
        assertTrue(result.stream().anyMatch(book -> book.getTitle().equals("El Hobbit")));
    }

    @Test
    @DisplayName("Debería buscar libros por autor con nombre completo")
    void shouldSearchBooksByFullAuthorName() {
        // When
        List<BookDto> result = bookController.searchByAuthor("Gabriel García Márquez");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(book -> book.getTitle().equals("Cien Años de Soledad")));
        assertTrue(result.stream().anyMatch(book -> book.getTitle().equals("Crónica de una Muerte Anunciada")));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el término de búsqueda por autor está vacío")
    void shouldThrowExceptionWhenAuthorSearchQueryIsEmpty() {
        // When/Then
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByAuthor(""));
        
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByAuthor(null));
    }

    @Test
    @DisplayName("Debería buscar libros por género correctamente")
    void shouldSearchBooksByGenre() {
        // When
        List<BookDto> result = bookController.searchByGenre("Fantasía");

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(book -> book.getGenre().equals("Fantasía")));
    }

    @Test
    @DisplayName("Debería buscar libros por género sin distinguir mayúsculas/minúsculas")
    void shouldSearchBooksByGenreCaseInsensitive() {
        // When
        List<BookDto> result = bookController.searchByGenre("fantasía");

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el género está vacío")
    void shouldThrowExceptionWhenGenreIsEmpty() {
        // When/Then
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByGenre(""));
        
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByGenre(null));
    }

    @Test
    @DisplayName("Debería buscar libro por ISBN correctamente")
    void shouldSearchBookByISBN() {
        // When
        BookDto result = bookController.searchByISBN("9780544003415");

        // Then
        assertNotNull(result);
        assertEquals("El Señor de los Anillos", result.getTitle());
        assertEquals("J.R.R. Tolkien", result.getAuthor());
        assertEquals("9780544003415", result.getIsbn());
    }

    @Test
    @DisplayName("Debería lanzar NotFoundException cuando no encuentra libro por ISBN")
    void shouldThrowNotFoundExceptionWhenISBNNotFound() {
        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            bookController.searchByISBN("9999999999999"));
        
        assertEquals("No se encontró libro con ISBN: 9999999999999", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el ISBN está vacío")
    void shouldThrowExceptionWhenISBNIsEmpty() {
        // When/Then
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByISBN(""));
        
        assertThrows(InvalidInputException.class, () -> 
            bookController.searchByISBN(null));
    }

    @Test
    @DisplayName("Debería obtener detalles de libro por ID correctamente")
    void shouldGetBookDetailsById() {
        // When
        BookDto result = bookController.getBookDetails(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals("El Señor de los Anillos", result.getTitle());
        assertEquals("J.R.R. Tolkien", result.getAuthor());
    }

    @Test
    @DisplayName("Debería lanzar NotFoundException cuando no encuentra libro por ID")
    void shouldThrowNotFoundExceptionWhenBookIdNotFound() {
        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            bookController.getBookDetails(999));
        
        assertEquals("No se encontró libro con ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Debería realizar búsqueda general en título, autor y género")
    void shouldPerformGeneralSearch() {
        // When - buscar término que aparece en títulos
        List<BookDto> titleResults = bookController.generalSearch("señor");
        
        // Then
        assertNotNull(titleResults);
        assertEquals(1, titleResults.size());
        assertEquals("El Señor de los Anillos", titleResults.get(0).getTitle());

        // When - buscar término que aparece en autores
        List<BookDto> authorResults = bookController.generalSearch("tolkien");
        
        // Then
        assertNotNull(authorResults);
        assertEquals(2, authorResults.size());

        // When - buscar término que aparece en géneros
        List<BookDto> genreResults = bookController.generalSearch("fantasía");
        
        // Then
        assertNotNull(genreResults);
        assertEquals(3, genreResults.size());
    }

    @Test
    @DisplayName("Debería realizar búsqueda general combinada")
    void shouldPerformCombinedGeneralSearch() {
        // When - buscar término que puede aparecer en múltiples campos
        List<BookDto> results = bookController.generalSearch("gabriel");

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(book -> 
            book.getAuthor().toLowerCase().contains("gabriel")));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando la búsqueda general está vacía")
    void shouldThrowExceptionWhenGeneralSearchQueryIsEmpty() {
        // When/Then
        assertThrows(InvalidInputException.class, () -> 
            bookController.generalSearch(""));
        
        assertThrows(InvalidInputException.class, () -> 
            bookController.generalSearch(null));
    }

    @Test
    @DisplayName("Debería retornar lista vacía en búsqueda general sin resultados")
    void shouldReturnEmptyListWhenGeneralSearchHasNoResults() {
        // When
        List<BookDto> result = bookController.generalSearch("inexistente");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debería obtener géneros disponibles en orden alfabético")
    void shouldGetAvailableGenresSorted() {
        // When
        List<String> result = bookController.getAvailableGenres();

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        // Orden alfabético real: Clásico, Distopía, Fantasía, Fábula, Realismo Mágico
        assertEquals("Clásico", result.get(0));
        assertEquals("Distopía", result.get(1));
        assertEquals("Fantasía", result.get(2));
        assertEquals("Fábula", result.get(3));
        assertEquals("Realismo Mágico", result.get(4));
    }

    @Test
    @DisplayName("Debería retornar géneros únicos sin duplicados")
    void shouldReturnUniqueGenres() {
        // When
        List<String> result = bookController.getAvailableGenres();

        // Then
        assertNotNull(result);
        assertEquals(result.size(), result.stream().distinct().count());
    }

    @Test
    @DisplayName("Debería manejar búsqueda por título con múltiples palabras")
    void shouldHandleMultiWordTitleSearch() {
        // When - buscar usando palabras que están en el título
        List<BookDto> result = bookController.searchByTitle("cien años");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cien Años de Soledad", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Debería manejar búsqueda parcial de autor")
    void shouldHandlePartialAuthorSearch() {
        // When
        List<BookDto> result = bookController.searchByAuthor("george");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1984", result.get(0).getTitle());
        assertEquals("George Orwell", result.get(0).getAuthor());
    }

    @Test
    @DisplayName("Debería validar que todos los libros del catálogo tienen datos completos")
    void shouldValidateAllCatalogBooksHaveCompleteData() {
        // When
        List<BookDto> allBooks = bookController.getAllBooks(0, 100);

        // Then
        assertNotNull(allBooks);
        assertEquals(8, allBooks.size());
        
        for (BookDto book : allBooks) {
            assertNotNull(book.getTitle(), "El título no debe ser null");
            assertNotNull(book.getAuthor(), "El autor no debe ser null");
            assertNotNull(book.getIsbn(), "El ISBN no debe ser null");
            assertNotNull(book.getGenre(), "El género no debe ser null");
            assertTrue(book.getBookId() > 0, "El ID debe ser positivo");
            assertFalse(book.getTitle().trim().isEmpty(), "El título no debe estar vacío");
            assertFalse(book.getAuthor().trim().isEmpty(), "El autor no debe estar vacío");
            assertFalse(book.getIsbn().trim().isEmpty(), "El ISBN no debe estar vacío");
            assertFalse(book.getGenre().trim().isEmpty(), "El género no debe estar vacío");
        }
    }
    
    @Test
    @DisplayName("Debería crear un nuevo libro exitosamente")
    void shouldCreateBookSuccessfully() {
        // Given
        BookDto newBook = new BookDto();
        newBook.setTitle("Nuevo Libro de Prueba");
        newBook.setAuthor("Autor de Prueba");
        newBook.setIsbn("9781234567890");
        newBook.setGenre("Ficción");
        
        // When
        var response = bookController.createBook(newBook);
        
        // Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Nuevo Libro de Prueba", response.getBody().getTitle());
        assertEquals("Autor de Prueba", response.getBody().getAuthor());
        assertEquals("9781234567890", response.getBody().getIsbn());
        assertEquals("Ficción", response.getBody().getGenre());
        assertTrue(response.getBody().getBookId() > 8); // Nuevo ID generado
    }
    
    @Test
    @DisplayName("Debería fallar al crear libro con título vacío")
    void shouldFailToCreateBookWithEmptyTitle() {
        // Given
        BookDto newBook = new BookDto();
        newBook.setTitle("");
        newBook.setAuthor("Autor de Prueba");
        newBook.setIsbn("9781234567891");
        newBook.setGenre("Ficción");
        
        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> bookController.createBook(newBook));
        assertEquals("El título del libro no puede estar vacío", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debería fallar al crear libro con autor vacío")
    void shouldFailToCreateBookWithEmptyAuthor() {
        // Given
        BookDto newBook = new BookDto();
        newBook.setTitle("Título de Prueba");
        newBook.setAuthor("");
        newBook.setIsbn("9781234567892");
        newBook.setGenre("Ficción");
        
        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> bookController.createBook(newBook));
        assertEquals("El autor del libro no puede estar vacío", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debería fallar al crear libro con ISBN vacío")
    void shouldFailToCreateBookWithEmptyIsbn() {
        // Given
        BookDto newBook = new BookDto();
        newBook.setTitle("Título de Prueba");
        newBook.setAuthor("Autor de Prueba");
        newBook.setIsbn("");
        newBook.setGenre("Ficción");
        
        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> bookController.createBook(newBook));
        assertEquals("El ISBN del libro no puede estar vacío", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debería fallar al crear libro con género vacío")
    void shouldFailToCreateBookWithEmptyGenre() {
        // Given
        BookDto newBook = new BookDto();
        newBook.setTitle("Título de Prueba");
        newBook.setAuthor("Autor de Prueba");
        newBook.setIsbn("9781234567893");
        newBook.setGenre("");
        
        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> bookController.createBook(newBook));
        assertEquals("El género del libro no puede estar vacío", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debería fallar al crear libro con ISBN duplicado")
    void shouldFailToCreateBookWithDuplicateIsbn() {
        // Given
        BookDto newBook = new BookDto();
        newBook.setTitle("Nuevo Libro");
        newBook.setAuthor("Nuevo Autor");
        newBook.setIsbn("9780544003415"); // ISBN existente (El Señor de los Anillos)
        newBook.setGenre("Ficción");
        
        // When & Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> bookController.createBook(newBook));
        assertEquals("Ya existe un libro con el ISBN: 9780544003415", exception.getMessage());
    }
    
    @Test
    @DisplayName("Debería crear libro y verificar que se añadió al catálogo")
    void shouldCreateBookAndVerifyItIsAddedToCatalog() {
        // Given
        BookDto newBook = new BookDto();
        newBook.setTitle("Libro de Verificación");
        newBook.setAuthor("Autor de Verificación");
        newBook.setIsbn("9781234567894");
        newBook.setGenre("Ficción Científica");
        
        // When
        var response = bookController.createBook(newBook);
        List<BookDto> allBooks = bookController.getAllBooks(0, 20);
        
        // Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(9, allBooks.size()); // Ahora debería haber 9 libros
        
        // Verificar que el nuevo libro está en el catálogo
        boolean bookFound = allBooks.stream()
            .anyMatch(book -> book.getIsbn().equals("9781234567894"));
        assertTrue(bookFound, "El nuevo libro debería estar en el catálogo");
    }
}
