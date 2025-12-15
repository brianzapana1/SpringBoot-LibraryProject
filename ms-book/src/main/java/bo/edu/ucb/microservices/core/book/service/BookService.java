package bo.edu.ucb.microservices.core.book.service;

import bo.edu.ucb.microservices.core.book.entity.Book;
import bo.edu.ucb.microservices.core.book.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {
    
    private static final Logger LOG = LoggerFactory.getLogger(BookService.class);
    
    @Autowired
    private BookRepository bookRepository;
    
    public List<Book> getAllBooks() {
        LOG.debug("Obteniendo todos los libros");
        return bookRepository.findAllByOrderByTitleAsc();
    }
    
    public Page<Book> getAllBooks(Pageable pageable) {
        LOG.debug("Obteniendo todos los libros con paginación: página {}, tamaño {}", 
                  pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable);
    }
    
    public Optional<Book> getBookById(Long id) {
        LOG.debug("Buscando libro por ID: {}", id);
        return bookRepository.findById(id);
    }
    
    public Optional<Book> getBookByIsbn(String isbn) {
        LOG.debug("Buscando libro por ISBN: {}", isbn);
        return bookRepository.findByIsbn(isbn);
    }
    
    public List<Book> getBooksByAuthor(String author) {
        LOG.debug("Buscando libros por autor: {}", author);
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }
    
    public List<Book> getBooksByTitle(String title) {
        LOG.debug("Buscando libros por título: {}", title);
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public List<Book> getBooksByGenre(String genre) {
        LOG.debug("Buscando libros por género: {}", genre);
        return bookRepository.findByGenreIgnoreCase(genre);
    }
    
    public List<Book> getAvailableBooks() {
        LOG.debug("Obteniendo libros disponibles");
        return bookRepository.findAvailableBooks();
    }
    
    // ========== MÉTODOS QUE USAN NATIVE QUERIES ==========
    
    public List<Book> getAvailableBooksNative() {
        LOG.debug("Obteniendo libros disponibles (Native Query)");
        return bookRepository.findAvailableBooksNative();
    }
    
    public List<Object[]> getBookStatisticsByGenre() {
        LOG.debug("Obteniendo estadísticas por género (Native Query)");
        return bookRepository.getBookStatisticsByGenre();
    }
    
    public List<Book> getMostBorrowedBooks(int limit) {
        LOG.debug("Obteniendo libros más prestados (Native Query)");
        return bookRepository.findMostBorrowedBooks(limit);
    }
    
    public List<Book> getBooksByYearRange(int startYear, int endYear) {
        LOG.debug("Obteniendo libros por rango de años: {} - {} (Native Query)", startYear, endYear);
        return bookRepository.findBooksByYearRange(startYear, endYear);
    }
    
    public List<Book> searchBooksFullText(String searchTerm) {
        LOG.debug("Búsqueda de texto completo: {} (Native Query)", searchTerm);
        return bookRepository.searchBooksFullText(searchTerm);
    }
    
    // ========== MÉTODOS QUE USAN DERIVED QUERIES ==========
    
    public List<Book> getBooksByDateAfter(LocalDate date) {
        LOG.debug("Obteniendo libros publicados después de: {} (Derived Query)", date);
        return bookRepository.findByPublicationDateAfter(date);
    }
    
    public List<Book> getBooksByPageRange(Integer minPages, Integer maxPages) {
        LOG.debug("Obteniendo libros con páginas entre {} y {} (Derived Query)", minPages, maxPages);
        return bookRepository.findByPageCountBetween(minPages, maxPages);
    }
    
    public List<String> getDistinctAuthors() {
        LOG.debug("Obteniendo autores únicos (JPQL Query)");
        return bookRepository.findDistinctAuthors();
    }
    
    // ========== MÉTODOS QUE USAN CRITERIA QUERIES ==========
    
    public List<Book> searchBooksWithCriteria(String title, String author, String genre, 
                                            LocalDate fromDate, LocalDate toDate,
                                            Integer minPages, Integer maxPages) {
        LOG.debug("Búsqueda avanzada con Criteria API");
        return bookRepository.findBooksWithCriteria(title, author, genre, fromDate, toDate, minPages, maxPages);
    }
    
    public Page<Book> searchBooksAdvanced(String searchTerm, String genre, 
                                        Boolean available, Pageable pageable) {
        LOG.debug("Búsqueda avanzada paginada con Criteria API");
        return bookRepository.searchBooksAdvanced(searchTerm, genre, available, pageable);
    }
    
    public Long countBooksByGenre(String genre) {
        LOG.debug("Contando libros por género: {} (JPQL Query)", genre);
        return bookRepository.countByGenre(genre);
    }
    
    // ========== MÉTODOS CRUD ORIGINALES ==========
    
    public Book saveBook(Book book) {
        LOG.debug("Guardando libro: {}", book);
        
        // Validar que no exista otro libro con el mismo ISBN
        if (book.getId() == null && bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("Ya existe un libro con el ISBN: " + book.getIsbn());
        }
        
        return bookRepository.save(book);
    }
    
    public Book updateBook(Long id, Book bookDetails) {
        LOG.debug("Actualizando libro ID: {} con detalles: {}", id, bookDetails);
        
        return bookRepository.findById(id)
            .map(book -> {
                book.setTitle(bookDetails.getTitle());
                book.setAuthor(bookDetails.getAuthor());
                book.setIsbn(bookDetails.getIsbn());
                book.setPublicationDate(bookDetails.getPublicationDate());
                book.setPageCount(bookDetails.getPageCount());
                book.setGenre(bookDetails.getGenre());
                book.setDescription(bookDetails.getDescription());
                book.setAvailableCopies(bookDetails.getAvailableCopies());
                book.setTotalCopies(bookDetails.getTotalCopies());
                
                return bookRepository.save(book);
            })
            .orElseThrow(() -> new RuntimeException("Libro no encontrado con ID: " + id));
    }
    
    public void deleteBook(Long id) {
        LOG.debug("Eliminando libro ID: {}", id);
        
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
        } else {
            throw new RuntimeException("Libro no encontrado con ID: " + id);
        }
    }
    
    public boolean borrowBook(Long id) {
        LOG.debug("Intentando prestar libro ID: {}", id);
        
        return bookRepository.findById(id)
            .map(book -> {
                if (book.getAvailableCopies() > 0) {
                    book.setAvailableCopies(book.getAvailableCopies() - 1);
                    bookRepository.save(book);
                    LOG.info("Libro prestado exitosamente. ID: {}, Copias restantes: {}", 
                            id, book.getAvailableCopies());
                    return true;
                } else {
                    LOG.warn("No hay copias disponibles del libro ID: {}", id);
                    return false;
                }
            })
            .orElse(false);
    }
    
    public boolean returnBook(Long id) {
        LOG.debug("Intentando devolver libro ID: {}", id);
        
        return bookRepository.findById(id)
            .map(book -> {
                if (book.getAvailableCopies() < book.getTotalCopies()) {
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                    bookRepository.save(book);
                    LOG.info("Libro devuelto exitosamente. ID: {}, Copias disponibles: {}", 
                            id, book.getAvailableCopies());
                    return true;
                } else {
                    LOG.warn("Todas las copias del libro ID: {} ya están disponibles", id);
                    return false;
                }
            })
            .orElse(false);
    }
}