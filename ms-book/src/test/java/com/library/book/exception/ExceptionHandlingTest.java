package com.library.book.exception;

import com.library.book.controller.BookController;
import com.library.util.exceptions.InvalidInputException;
import com.library.util.exceptions.NotFoundException;
import com.library.util.http.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Exception Handling Tests")
class ExceptionHandlingTest {

    @Mock
    private ServiceUtil serviceUtil;

    private BookController bookController;

    @BeforeEach
    void setUp() {
        bookController = new BookController(serviceUtil);
    }

    @Test
    @DisplayName("Debería lanzar InvalidInputException para búsqueda por título vacía")
    void shouldThrowInvalidInputExceptionForEmptyTitleSearch() {
        // Test con cadena vacía
        InvalidInputException exception1 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByTitle(""));
        assertEquals("El término de búsqueda no puede estar vacío", exception1.getMessage());

        // Test con solo espacios
        InvalidInputException exception2 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByTitle("   "));
        assertEquals("El término de búsqueda no puede estar vacío", exception2.getMessage());

        // Test con null
        InvalidInputException exception3 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByTitle(null));
        assertEquals("El término de búsqueda no puede estar vacío", exception3.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar InvalidInputException para búsqueda por autor vacía")
    void shouldThrowInvalidInputExceptionForEmptyAuthorSearch() {
        // Test con cadena vacía
        InvalidInputException exception1 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByAuthor(""));
        assertEquals("El término de búsqueda no puede estar vacío", exception1.getMessage());

        // Test con solo espacios
        InvalidInputException exception2 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByAuthor("   "));
        assertEquals("El término de búsqueda no puede estar vacío", exception2.getMessage());

        // Test con null
        InvalidInputException exception3 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByAuthor(null));
        assertEquals("El término de búsqueda no puede estar vacío", exception3.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar InvalidInputException para búsqueda por género vacía")
    void shouldThrowInvalidInputExceptionForEmptyGenreSearch() {
        // Test con cadena vacía
        InvalidInputException exception1 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByGenre(""));
        assertEquals("El género no puede estar vacío", exception1.getMessage());

        // Test con solo espacios
        InvalidInputException exception2 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByGenre("   "));
        assertEquals("El género no puede estar vacío", exception2.getMessage());

        // Test con null
        InvalidInputException exception3 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByGenre(null));
        assertEquals("El género no puede estar vacío", exception3.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar InvalidInputException para búsqueda por ISBN vacía")
    void shouldThrowInvalidInputExceptionForEmptyISBNSearch() {
        // Test con cadena vacía
        InvalidInputException exception1 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByISBN(""));
        assertEquals("El ISBN no puede estar vacío", exception1.getMessage());

        // Test con solo espacios
        InvalidInputException exception2 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByISBN("   "));
        assertEquals("El ISBN no puede estar vacío", exception2.getMessage());

        // Test con null
        InvalidInputException exception3 = assertThrows(InvalidInputException.class, 
            () -> bookController.searchByISBN(null));
        assertEquals("El ISBN no puede estar vacío", exception3.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar InvalidInputException para búsqueda general vacía")
    void shouldThrowInvalidInputExceptionForEmptyGeneralSearch() {
        // Test con cadena vacía
        InvalidInputException exception1 = assertThrows(InvalidInputException.class, 
            () -> bookController.generalSearch(""));
        assertEquals("El término de búsqueda no puede estar vacío", exception1.getMessage());

        // Test con solo espacios
        InvalidInputException exception2 = assertThrows(InvalidInputException.class, 
            () -> bookController.generalSearch("   "));
        assertEquals("El término de búsqueda no puede estar vacío", exception2.getMessage());

        // Test con null
        InvalidInputException exception3 = assertThrows(InvalidInputException.class, 
            () -> bookController.generalSearch(null));
        assertEquals("El término de búsqueda no puede estar vacío", exception3.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar NotFoundException para ISBN inexistente")
    void shouldThrowNotFoundExceptionForNonExistentISBN() {
        String nonExistentISBN = "9999999999999";
        
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> bookController.searchByISBN(nonExistentISBN));
        
        assertEquals("No se encontró libro con ISBN: " + nonExistentISBN, exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar NotFoundException para ID de libro inexistente")
    void shouldThrowNotFoundExceptionForNonExistentBookId() {
        int nonExistentId = 999;
        
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> bookController.getBookDetails(nonExistentId));
        
        assertEquals("No se encontró libro con ID: " + nonExistentId, exception.getMessage());
    }

    @Test
    @DisplayName("Debería validar que las excepciones contienen mensajes descriptivos")
    void shouldValidateExceptionMessages() {
        // Verificar que los mensajes de excepción son descriptivos y útiles
        try {
            bookController.searchByTitle("");
            fail("Debería haber lanzado InvalidInputException");
        } catch (InvalidInputException e) {
            assertNotNull(e.getMessage());
            assertFalse(e.getMessage().isEmpty());
            assertTrue(e.getMessage().contains("término de búsqueda"));
        }

        try {
            bookController.searchByISBN("0000000000000");
            fail("Debería haber lanzado NotFoundException");
        } catch (NotFoundException e) {
            assertNotNull(e.getMessage());
            assertFalse(e.getMessage().isEmpty());
            assertTrue(e.getMessage().contains("No se encontró"));
            assertTrue(e.getMessage().contains("ISBN"));
        }
    }

    @Test
    @DisplayName("Debería manejar diferentes formatos de input inválido")
    void shouldHandleDifferentInvalidInputFormats() {
        // Strings con solo espacios en blanco
        assertThrows(InvalidInputException.class, 
            () -> bookController.searchByTitle("   \t  \n  "));

        // Strings con caracteres de control
        assertThrows(InvalidInputException.class, 
            () -> bookController.searchByAuthor("\r\n"));

        // Empty genre
        assertThrows(InvalidInputException.class, 
            () -> bookController.searchByGenre("\t"));
    }

    @Test
    @DisplayName("Debería validar que no se lanzan excepciones para inputs válidos")
    void shouldNotThrowExceptionsForValidInputs() {
        // Estas llamadas no deberían lanzar excepciones
        assertDoesNotThrow(() -> bookController.searchByTitle("test"));
        assertDoesNotThrow(() -> bookController.searchByAuthor("test"));
        assertDoesNotThrow(() -> bookController.searchByGenre("test"));
        assertDoesNotThrow(() -> bookController.generalSearch("test"));
        assertDoesNotThrow(() -> bookController.getAllBooks(0, 10));
        assertDoesNotThrow(() -> bookController.getAvailableGenres());
    }

    @Test
    @DisplayName("Debería manejar casos edge de paginación sin lanzar excepciones")
    void shouldHandlePaginationEdgeCasesWithoutExceptions() {
        // Página muy grande
        assertDoesNotThrow(() -> bookController.getAllBooks(1000, 10));
        
        // Tamaño de página muy grande
        assertDoesNotThrow(() -> bookController.getAllBooks(0, 1000));
        
        // Página 0 con tamaño 1
        assertDoesNotThrow(() -> bookController.getAllBooks(0, 1));
    }

    @Test
    @DisplayName("Debería validar tipos de excepción correctos")
    void shouldValidateCorrectExceptionTypes() {
        // Verificar que se lanzan los tipos correctos de excepción
        Exception titleException = assertThrows(Exception.class, 
            () -> bookController.searchByTitle(""));
        assertTrue(titleException instanceof InvalidInputException);

        Exception isbnException = assertThrows(Exception.class, 
            () -> bookController.searchByISBN("0000000000000"));
        assertTrue(isbnException instanceof NotFoundException);

        Exception idException = assertThrows(Exception.class, 
            () -> bookController.getBookDetails(999));
        assertTrue(idException instanceof NotFoundException);
    }

    @Test
    @DisplayName("Debería preservar el stack trace en las excepciones")
    void shouldPreserveStackTraceInExceptions() {
        try {
            bookController.searchByTitle("");
        } catch (InvalidInputException e) {
            assertNotNull(e.getStackTrace());
            assertTrue(e.getStackTrace().length > 0);
            assertTrue(e.getStackTrace()[0].getClassName().contains("BookController"));
        }

        try {
            bookController.getBookDetails(999);
        } catch (NotFoundException e) {
            assertNotNull(e.getStackTrace());
            assertTrue(e.getStackTrace().length > 0);
            assertTrue(e.getStackTrace()[0].getClassName().contains("BookController"));
        }
    }
}
