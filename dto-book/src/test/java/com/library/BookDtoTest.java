package com.library;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookDtoTest {
    @Test
    void testBookDtoCreation() {
        BookDto book = new BookDto(1, "El libro misterioso", "978-1-492-00824-9", "Autor ABC", "Fantasia");
        assertEquals(1, book.getBookId());
        assertEquals("El libro misterioso", book.getTitle());
        assertEquals("978-1-492-00824-9", book.getIsbn());
        assertEquals("Autor ABC", book.getAuthor());
        assertEquals("Fantasia", book.getGenre());
    }
}
