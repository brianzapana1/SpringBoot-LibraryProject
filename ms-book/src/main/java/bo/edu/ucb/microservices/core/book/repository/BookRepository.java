package bo.edu.ucb.microservices.core.book.repository;

import bo.edu.ucb.microservices.core.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {
    
    // =================== DERIVED QUERIES ===================
    // Estas son consultas derivadas automaticamente del nombre del método
    
    // Buscar libro por ISBN
    Optional<Book> findByIsbn(String isbn);
    
    // Buscar libros por autor
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // Buscar libros por título
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Buscar libros por género
    List<Book> findByGenreIgnoreCase(String genre);
    
    // Buscar libros por autor y género
    List<Book> findByAuthorContainingIgnoreCaseAndGenreIgnoreCase(String author, String genre);
    
    // Verificar si existe un libro con ese ISBN
    boolean existsByIsbn(String isbn);
    
    // Buscar libros ordenados por título
    List<Book> findAllByOrderByTitleAsc();
    
    // Buscar libros por fecha de publicación después de una fecha específica
    List<Book> findByPublicationDateAfter(LocalDate date);
    
    // Buscar libros por número de páginas entre dos valores
    List<Book> findByPageCountBetween(Integer minPages, Integer maxPages);
    
    // Buscar libros con copias disponibles mayor a cero
    List<Book> findByAvailableCopiesGreaterThan(Integer copies);
    
    // =================== JPQL QUERIES ===================
    // Estas son consultas JPQL (Java Persistence Query Language)
    
    // Buscar libros disponibles
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();
    
    // Buscar libros con copias disponibles mayor a un número específico
    @Query("SELECT b FROM Book b WHERE b.availableCopies >= :minCopies")
    List<Book> findBooksWithMinimumCopies(@Param("minCopies") Integer minCopies);
    
    // Buscar libros por múltiples campos con JPQL
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:genre IS NULL OR LOWER(b.genre) = LOWER(:genre))")
    List<Book> findByMultipleFields(@Param("title") String title, 
                                   @Param("author") String author, 
                                   @Param("genre") String genre);
    
    // Contar libros por género
    @Query("SELECT COUNT(b) FROM Book b WHERE b.genre = :genre")
    Long countByGenre(@Param("genre") String genre);
    
    // Obtener autores únicos
    @Query("SELECT DISTINCT b.author FROM Book b ORDER BY b.author")
    List<String> findDistinctAuthors();
    
    // =================== NATIVE QUERIES ===================
    // Estas son consultas SQL nativas que acceden directamente a la base de datos
    
    // Buscar libros con consulta SQL nativa
    @Query(value = "SELECT * FROM books WHERE available_copies > 0 ORDER BY title", 
           nativeQuery = true)
    List<Book> findAvailableBooksNative();
    
    // Estadísticas de libros por género usando SQL nativo
    @Query(value = "SELECT genre, COUNT(*) as total, SUM(available_copies) as available " +
                   "FROM books GROUP BY genre ORDER BY total DESC", 
           nativeQuery = true)
    List<Object[]> getBookStatisticsByGenre();
    
    // Buscar libros populares (con menos copias disponibles que el total)
    @Query(value = "SELECT * FROM books WHERE available_copies < total_copies " +
                   "ORDER BY (total_copies - available_copies) DESC LIMIT :limit", 
           nativeQuery = true)
    List<Book> findMostBorrowedBooks(@Param("limit") int limit);
    
    // Buscar libros publicados en un rango de años
    @Query(value = "SELECT * FROM books WHERE EXTRACT(YEAR FROM publication_date) " +
                   "BETWEEN :startYear AND :endYear ORDER BY publication_date DESC", 
           nativeQuery = true)
    List<Book> findBooksByYearRange(@Param("startYear") int startYear, 
                                   @Param("endYear") int endYear);
    
    // Actualizar copias disponibles con consulta nativa
    @Modifying
    @Query(value = "UPDATE books SET available_copies = :copies WHERE id = :bookId", 
           nativeQuery = true)
    int updateAvailableCopies(@Param("bookId") Long bookId, @Param("copies") Integer copies);
    
    // Buscar libros con texto completo (búsqueda en título, autor y descripción)
    @Query(value = "SELECT * FROM books WHERE " +
                   "LOWER(title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                   "LOWER(author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                   "LOWER(description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))", 
           nativeQuery = true)
    List<Book> searchBooksFullText(@Param("searchTerm") String searchTerm);
}