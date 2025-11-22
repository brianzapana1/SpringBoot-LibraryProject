package bo.edu.ucb.microservices.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Schema(description = "DTO que representa un libro")
public class BookDto {
    @Schema(description = "ID del libro", example = "1")
    private int bookId;

    @Schema(description = "Título del libro", example = "El libro misterioso")
    @NotNull(message = "El título no puede ser nulo")
    @Size(min = 2, max = 100, message = "El título debe tener entre 2 y 100 caracteres")
    private String title;

    @Schema(description = "Autor del libro", example = "Autor ABC")
    @NotNull(message = "El autor no puede ser nulo")
    @Size(min = 2, max = 50, message = "El autor debe tener entre 2 y 50 caracteres")
    private String author;

    @Schema(description = "ISBN del libro", example = "978-1-492-00824-9")
    @NotNull(message = "El ISBN no puede ser nulo")
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "El ISBN debe ser válido")
    private String isbn;

    @Schema(description = "Género del libro", example = "Fantasia")
    @NotNull(message = "El género no puede ser nulo")
    @Size(min = 3, max = 30, message = "El género debe tener entre 3 y 30 caracteres")
    private String genre;

    // Constructor por defecto necesario para Jackson
    public BookDto() {
    }

    public BookDto(int bookId, String title, String isbn, String author, String genre) {
        super();
        this.bookId = bookId;
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.genre = genre;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "BookDto{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }
}