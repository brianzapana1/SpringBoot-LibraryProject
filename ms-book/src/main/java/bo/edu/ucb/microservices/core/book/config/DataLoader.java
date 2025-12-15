package bo.edu.ucb.microservices.core.book.config;

import bo.edu.ucb.microservices.core.book.entity.Book;
import bo.edu.ucb.microservices.core.book.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private BookRepository bookRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOG.info("Inicializando datos de ejemplo en la base de datos...");
        loadSampleBooks();
        LOG.info("Datos de ejemplo cargados exitosamente");
    }

    private void loadSampleBooks() {
        // Verificar si ya hay datos
        if (bookRepository.count() > 0) {
            LOG.info("La base de datos ya contiene datos. No se cargarán datos de ejemplo.");
            return;
        }

        // Crear libros de ejemplo
        Book[] sampleBooks = {
            createBook("El Señor de los Anillos", "J.R.R. Tolkien", "9780544003415", 
                      LocalDate.of(1954, 7, 29), 1216, "Fantasía", 
                      "Una épica aventura de fantasía en la Tierra Media", 5, 5),
                      
            createBook("Cien Años de Soledad", "Gabriel García Márquez", "9780307474728", 
                      LocalDate.of(1967, 5, 30), 417, "Realismo Mágico", 
                      "La historia multigeneracional de la familia Buendía", 3, 3),
                      
            createBook("1984", "George Orwell", "9780451524935", 
                      LocalDate.of(1949, 6, 8), 328, "Distopía", 
                      "Una novela distópica sobre un futuro totalitario", 4, 4),
                      
            createBook("Don Quijote de la Mancha", "Miguel de Cervantes", "9788491050940", 
                      LocalDate.of(1605, 1, 16), 863, "Clásico", 
                      "Las aventuras del ingenioso hidalgo Don Quijote", 2, 2),
                      
            createBook("Harry Potter y la Piedra Filosofal", "J.K. Rowling", "9788498383440", 
                      LocalDate.of(1997, 6, 26), 223, "Fantasía", 
                      "El primer libro de la saga del joven mago", 6, 6),
                      
            createBook("El Principito", "Antoine de Saint-Exupéry", "9780156012195", 
                      LocalDate.of(1943, 4, 6), 96, "Fábula", 
                      "Una fábula poética sobre la amistad y el amor", 8, 8),
                      
            createBook("Crónica de una Muerte Anunciada", "Gabriel García Márquez", "9780307388285", 
                      LocalDate.of(1981, 1, 1), 122, "Realismo Mágico", 
                      "Una crónica sobre un asesinato anunciado", 3, 3),
                      
            createBook("El Hobbit", "J.R.R. Tolkien", "9780547928227", 
                      LocalDate.of(1937, 9, 21), 310, "Fantasía", 
                      "La aventura de Bilbo Bolsón hacia la Montaña Solitaria", 4, 4),
                      
            createBook("Orgullo y Prejuicio", "Jane Austen", "9780141439518", 
                      LocalDate.of(1813, 1, 28), 279, "Romance", 
                      "Una novela romántica sobre Elizabeth Bennet y Mr. Darcy", 3, 3),
                      
            createBook("El Código Da Vinci", "Dan Brown", "9780307474278", 
                      LocalDate.of(2003, 3, 18), 454, "Thriller", 
                      "Un thriller sobre símbolos, religión y conspiración", 2, 2)
        };

        // Guardar todos los libros
        for (Book book : sampleBooks) {
            bookRepository.save(book);
            LOG.debug("Libro guardado: {}", book.getTitle());
        }

        LOG.info("Se han cargado {} libros de ejemplo", sampleBooks.length);
    }

    private Book createBook(String title, String author, String isbn, LocalDate publicationDate, 
                           Integer pageCount, String genre, String description, 
                           Integer totalCopies, Integer availableCopies) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublicationDate(publicationDate);
        book.setPageCount(pageCount);
        book.setGenre(genre);
        book.setDescription(description);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);
        return book;
    }
}