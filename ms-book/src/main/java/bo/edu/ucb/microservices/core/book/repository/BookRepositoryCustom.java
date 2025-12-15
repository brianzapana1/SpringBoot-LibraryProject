package bo.edu.ucb.microservices.core.book.repository;

import bo.edu.ucb.microservices.core.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio personalizado para consultas con Criteria API
 * Permite construir consultas dinámicas de forma programática
 */
public interface BookRepositoryCustom {
    
    /**
     * Buscar libros con criterios múltiples usando Criteria Query
     */
    List<Book> findBooksWithCriteria(String title, String author, String genre, 
                                    LocalDate fromDate, LocalDate toDate,
                                    Integer minPages, Integer maxPages);
    
    /**
     * Búsqueda avanzada con paginación usando Criteria API
     */
    Page<Book> searchBooksAdvanced(String searchTerm, String genre, 
                                 Boolean available, Pageable pageable);
    
    /**
     * Contar libros con criterios específicos
     */
    Long countBooksWithCriteria(String genre, Boolean hasDescription);
    
    /**
     * Buscar libros ordenados dinámicamente
     */
    List<Book> findBooksWithDynamicSorting(String sortBy, String sortDirection, 
                                          Boolean availableOnly);
}