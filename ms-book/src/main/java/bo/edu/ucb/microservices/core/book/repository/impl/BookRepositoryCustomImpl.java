package bo.edu.ucb.microservices.core.book.repository.impl;

import bo.edu.ucb.microservices.core.book.entity.Book;
import bo.edu.ucb.microservices.core.book.repository.BookRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del repositorio personalizado usando Criteria API
 * Estas son consultas construidas programáticamente para mayor flexibilidad
 */
@Repository
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Book> findBooksWithCriteria(String title, String author, String genre, 
                                           LocalDate fromDate, LocalDate toDate,
                                           Integer minPages, Integer maxPages) {
        
        // 1. Crear CriteriaBuilder
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // 2. Crear CriteriaQuery
        CriteriaQuery<Book> query = cb.createQuery(Book.class);
        
        // 3. Definir Root (FROM)
        Root<Book> book = query.from(Book.class);
        
        // 4. Crear lista de predicados (WHERE)
        List<Predicate> predicates = new ArrayList<>();
        
        // Filtro por título
        if (title != null && !title.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(book.get("title")), 
                                 "%" + title.toLowerCase() + "%"));
        }
        
        // Filtro por autor
        if (author != null && !author.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(book.get("author")), 
                                 "%" + author.toLowerCase() + "%"));
        }
        
        // Filtro por género
        if (genre != null && !genre.trim().isEmpty()) {
            predicates.add(cb.equal(cb.lower(book.get("genre")), 
                                  genre.toLowerCase()));
        }
        
        // Filtro por fecha de publicación (desde)
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(book.get("publicationDate"), fromDate));
        }
        
        // Filtro por fecha de publicación (hasta)
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(book.get("publicationDate"), toDate));
        }
        
        // Filtro por páginas mínimas
        if (minPages != null) {
            predicates.add(cb.greaterThanOrEqualTo(book.get("pageCount"), minPages));
        }
        
        // Filtro por páginas máximas
        if (maxPages != null) {
            predicates.add(cb.lessThanOrEqualTo(book.get("pageCount"), maxPages));
        }
        
        // 5. Construir la consulta
        query.select(book)
             .where(cb.and(predicates.toArray(new Predicate[0])))
             .orderBy(cb.asc(book.get("title")));
        
        // 6. Ejecutar consulta
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Page<Book> searchBooksAdvanced(String searchTerm, String genre, 
                                        Boolean available, Pageable pageable) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> query = cb.createQuery(Book.class);
        Root<Book> book = query.from(Book.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Búsqueda en múltiples campos
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate titleMatch = cb.like(cb.lower(book.get("title")), searchPattern);
            Predicate authorMatch = cb.like(cb.lower(book.get("author")), searchPattern);
            Predicate descMatch = cb.like(cb.lower(book.get("description")), searchPattern);
            
            predicates.add(cb.or(titleMatch, authorMatch, descMatch));
        }
        
        // Filtro por género
        if (genre != null && !genre.trim().isEmpty()) {
            predicates.add(cb.equal(cb.lower(book.get("genre")), genre.toLowerCase()));
        }
        
        // Filtro por disponibilidad
        if (available != null && available) {
            predicates.add(cb.greaterThan(book.get("availableCopies"), 0));
        }
        
        // Construir consulta principal
        query.select(book)
             .where(cb.and(predicates.toArray(new Predicate[0])));
        
        // Agregar ordenamiento dinámico
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(book.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(book.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        } else {
            query.orderBy(cb.asc(book.get("title")));
        }
        
        // Ejecutar consulta con paginación
        TypedQuery<Book> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Book> books = typedQuery.getResultList();
        
        // Contar total de registros
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Book> countRoot = countQuery.from(Book.class);
        countQuery.select(cb.count(countRoot))
                 .where(cb.and(predicates.toArray(new Predicate[0])));
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(books, pageable, total);
    }

    @Override
    public Long countBooksWithCriteria(String genre, Boolean hasDescription) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Book> book = query.from(Book.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (genre != null && !genre.trim().isEmpty()) {
            predicates.add(cb.equal(cb.lower(book.get("genre")), genre.toLowerCase()));
        }
        
        if (hasDescription != null) {
            if (hasDescription) {
                predicates.add(cb.isNotNull(book.get("description")));
                predicates.add(cb.notEqual(book.get("description"), ""));
            } else {
                predicates.add(cb.or(
                    cb.isNull(book.get("description")),
                    cb.equal(book.get("description"), "")
                ));
            }
        }
        
        query.select(cb.count(book))
             .where(cb.and(predicates.toArray(new Predicate[0])));
        
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<Book> findBooksWithDynamicSorting(String sortBy, String sortDirection, 
                                                 Boolean availableOnly) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> query = cb.createQuery(Book.class);
        Root<Book> book = query.from(Book.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Filtrar solo libros disponibles si se especifica
        if (availableOnly != null && availableOnly) {
            predicates.add(cb.greaterThan(book.get("availableCopies"), 0));
        }
        
        // Aplicar filtros
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Aplicar ordenamiento dinámico
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Order order;
            if ("DESC".equalsIgnoreCase(sortDirection)) {
                order = cb.desc(book.get(sortBy));
            } else {
                order = cb.asc(book.get(sortBy));
            }
            query.orderBy(order);
        } else {
            query.orderBy(cb.asc(book.get("title")));
        }
        
        return entityManager.createQuery(query).getResultList();
    }
}