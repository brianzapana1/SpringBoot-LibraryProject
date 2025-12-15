# Database Integration and Query Types Implementation

## ✅ COMPLETED SETUP

### 1. PostgreSQL Database Setup with Docker
- **Status**: ✅ FUNCTIONAL
- **Location**: `docker-compose.yml`
- **Database**: PostgreSQL 15 running on localhost:5432
- **Credentials**: library/library@librarydb
- **Sample Data**: 21 books across 15 different genres

### 2. Application Database Connection
- **Status**: ✅ FUNCTIONAL
- **Configuration**: `application-dev.yml`
- **Connection Pool**: HikariCP successfully connecting to PostgreSQL
- **JPA/Hibernate**: Properly configured with PostgreSQL dialect

### 3. Repository Query Types Implementation
All three required query types have been successfully implemented:

## 📋 IMPLEMENTED QUERY TYPES

### 1. DERIVED QUERIES (Spring Data JPA Method Names)
```java
// These queries are automatically generated from method names
Optional<Book> findByIsbn(String isbn);
List<Book> findByAuthorContainingIgnoreCase(String author);
List<Book> findByTitleContainingIgnoreCase(String title);
List<Book> findByGenreIgnoreCase(String genre);
List<Book> findByAuthorContainingIgnoreCaseAndGenreIgnoreCase(String author, String genre);
boolean existsByIsbn(String isbn);
List<Book> findAllByOrderByTitleAsc();
List<Book> findByPublicationDateAfter(LocalDate date);
List<Book> findByPageCountBetween(Integer minPages, Integer maxPages);
List<Book> findByAvailableCopiesGreaterThan(Integer copies);
```

### 2. JPQL QUERIES (Java Persistence Query Language)
```java
// Custom JPQL queries for more complex operations
@Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
List<Book> findAvailableBooks();

@Query("SELECT b FROM Book b WHERE b.availableCopies >= :minCopies")
List<Book> findBooksWithMinimumCopies(@Param("minCopies") Integer minCopies);

@Query("SELECT b FROM Book b WHERE " +
       "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
       "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
       "(:genre IS NULL OR LOWER(b.genre) = LOWER(:genre))")
List<Book> findByMultipleFields(@Param("title") String title, 
                               @Param("author") String author, 
                               @Param("genre") String genre);

@Query("SELECT COUNT(b) FROM Book b WHERE b.genre = :genre")
Long countByGenre(@Param("genre") String genre);

@Query("SELECT DISTINCT b.author FROM Book b ORDER BY b.author")
List<String> findDistinctAuthors();
```

### 3. NATIVE QUERIES (Direct SQL)
```java
// Raw SQL queries for database-specific operations
@Query(value = "SELECT * FROM books WHERE available_copies > 0 ORDER BY title", 
       nativeQuery = true)
List<Book> findAvailableBooksNative();

@Query(value = "SELECT genre, COUNT(*) as total, SUM(available_copies) as available " +
               "FROM books GROUP BY genre ORDER BY total DESC", 
       nativeQuery = true)
List<Object[]> getBookStatisticsByGenre();

@Query(value = "SELECT * FROM books WHERE available_copies < total_copies " +
               "ORDER BY (total_copies - available_copies) DESC LIMIT :limit", 
       nativeQuery = true)
List<Book> findMostBorrowedBooks(@Param("limit") int limit);

@Query(value = "SELECT * FROM books WHERE EXTRACT(YEAR FROM publication_date) " +
               "BETWEEN :startYear AND :endYear ORDER BY publication_date DESC", 
       nativeQuery = true)
List<Book> findBooksByYearRange(@Param("startYear") int startYear, 
                               @Param("endYear") int endYear);

@Query(value = "SELECT * FROM books WHERE " +
               "LOWER(title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
               "LOWER(author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
               "LOWER(description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))", 
       nativeQuery = true)
List<Book> searchBooksFullText(@Param("searchTerm") String searchTerm);
```

### 4. CRITERIA QUERIES (Programmatic API)
```java
// Dynamic queries built programmatically
// Implemented in BookRepositoryCustomImpl class
public List<Book> findBooksWithCriteria(String title, String author, String genre, 
                                       LocalDate fromDate, LocalDate toDate,
                                       Integer minPages, Integer maxPages);

public Page<Book> searchBooksAdvanced(String searchTerm, String genre, 
                                     Boolean available, Pageable pageable);

public Long countBooksWithCriteria(String genre, Boolean hasDescription);

public List<Book> findBooksWithDynamicSorting(String sortBy, String sortDirection, 
                                             Boolean availableOnly);
```

## 🛠 API ENDPOINTS IMPLEMENTATION

### Gateway Access URLs (Recommended)
**Base URL**: `http://localhost:8090`

### Basic CRUD Operations
- `GET /api/books` - Paginated list of all books
- `GET /api/books/all` - All books without pagination
- `GET /api/books/{id}` - Get book by ID
- `POST /api/books` - Create new book
- `PUT /api/books/{id}` - Update existing book
- `DELETE /api/books/{id}` - Delete book

### Query Type Demonstrations
- `GET /api/search/title?query=...` - **DERIVED QUERY**: Search by title
- `GET /api/search/author?query=...` - **DERIVED QUERY**: Search by author
- `GET /api/search/genre?genre=...` - **DERIVED QUERY**: Search by genre
- `GET /api/available` - **JPQL QUERY**: Get available books
- `GET /api/authors` - **JPQL QUERY**: Get unique authors
- `GET /api/statistics/genre` - **NATIVE QUERY**: Genre statistics
- `GET /api/popular?limit=...` - **NATIVE QUERY**: Most borrowed books
- `GET /api/search/fulltext?query=...` - **NATIVE QUERY**: Full text search
- `GET /api/search/pages?minPages=...&maxPages=...` - **DERIVED QUERY**: Page range
- `GET /api/search/advanced?title=...&author=...&genre=...` - **CRITERIA QUERY**: Advanced search
- `GET /api/search/advanced/paginated?searchTerm=...` - **CRITERIA QUERY**: Advanced search with pagination

### Business Operations
- `POST /api/books/{id}/borrow` - Borrow book
- `POST /api/books/{id}/return` - Return book
- `GET /api/health` - Health check
- `GET /api/info` - Service information

### Swagger UI Access
- **Swagger UI**: `http://localhost:8090/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:8090/v3/api-docs`
- **Direct Swagger**: `http://localhost:8090/webjars/swagger-ui/index.html`

### Direct MS-Book Access (Internal)
**Base URL**: `http://localhost:8082` (for development only)
- All endpoints available with `/ms-book/v1/api/` prefix

## 🧪 API EXAMPLES

### Basic Operations
```bash
# Get all books with pagination
curl "http://localhost:8090/api/books?page=0&size=5"

# Get a specific book
curl "http://localhost:8090/api/books/1"

# Create a new book
curl -X POST "http://localhost:8090/api/books" \
     -H "Content-Type: application/json" \
     -d '{"title":"New Book","author":"Author Name","isbn":"1234567890123"}'
```

### Search Operations
```bash
# Search by title
curl "http://localhost:8090/api/search/title?query=quijote"

# Search by author  
curl "http://localhost:8090/api/search/author?query=García"

# Search by genre
curl "http://localhost:8090/api/search/genre?genre=Fantasía"

# Full text search
curl "http://localhost:8090/api/search/fulltext?query=aventura"

# Advanced search
curl "http://localhost:8090/api/search/advanced?title=señor&author=tolkien"
```

### Statistics and Analytics
```bash
# Genre statistics
curl "http://localhost:8090/api/statistics/genre"

# Most popular books
curl "http://localhost:8090/api/popular?limit=5"

# Get all unique authors
curl "http://localhost:8090/api/authors"

# Get available books only
curl "http://localhost:8090/api/available"
```

### Business Operations
```bash
# Borrow a book
curl -X POST "http://localhost:8090/api/books/1/borrow"

# Return a book
curl -X POST "http://localhost:8090/api/books/1/return"

# Health check
curl "http://localhost:8090/api/health"
```

## ⚠️ IMPORTANT NOTES FOR CRUD OPERATIONS

### Validation Rules:
- **ISBN**: Must be unique across all books
- **Title**: Required field, cannot be null
- **Author**: Required field, cannot be null
- **ID**: Auto-generated for new books (don't include in POST requests)

### HTTP Status Codes:
- **200 OK**: Successful GET, PUT operations
- **201 Created**: Successful POST (book creation)
- **204 No Content**: Successful DELETE
- **400 Bad Request**: Invalid request data
- **404 Not Found**: Book not found
- **409 Conflict**: ISBN already exists (duplicate)

### Tips for Swagger UI Testing:
1. **For POST**: Don't include `id` field - it's auto-generated
2. **For PUT**: Include all fields including `id`
3. **For DELETE**: Only need the book ID in the URL
4. **ISBN uniqueness**: Use different ISBN for each test book
5. **Date format**: Use "YYYY-MM-DD" format for `publicationDate`

## 📊 DATABASE CONTENT

The PostgreSQL database contains 21 sample books including:
- **Literature**: Don Quijote, Cien años de soledad, 1984
- **Science Fiction**: Dune, Neuromante, El fin de la eternidad
- **Fantasy**: El Señor de los Anillos, Juego de Tronos, El Nombre del Viento
- **Programming**: Clean Code, Design Patterns, Effective Java
- **Science**: Una breve historia del tiempo, El origen de las especies, Cosmos
- **Philosophy**: Así habló Zaratustra, El mundo de Sofia
- **Self-help**: Los 7 hábitos, Atomic Habits
- **History**: Sapiens, El Arte de la Guerra

Each book has realistic data including:
- Title, Author, ISBN
- Publication Date, Page Count, Genre
- Description
- Available/Total Copies for lending system

## 🚀 HOW TO RUN

1. **Start PostgreSQL**:
   ```bash
   cd "SpringBoot-LibraryProject-main"
   docker-compose up -d
   ```

2. **Run the Application**:
   ```bash
   cd ms-book
   java -Dspring.profiles.active=dev -jar target/ms-book-0.0.1-SNAPSHOT.jar
   ```

3. **Test the Endpoints**:
   ```bash
   # Run the comprehensive test script
   ./test-api.ps1
   
   # Or test individual endpoints
   curl http://localhost:8082/books/all
   curl http://localhost:8082/search/author?query=García
   curl http://localhost:8082/statistics/genre
   ```

## ✅ VERIFICATION CHECKLIST

- [x] **Database Connection**: PostgreSQL container running and accessible
- [x] **Application Startup**: Spring Boot app successfully connects to database
- [x] **Data Loading**: 21 books loaded from initialization script
- [x] **CRUD Operations**: ✅ ALL CRUD operations working perfectly through Gateway
  - [x] **GET /api/books** - ✅ Paginated list working
  - [x] **GET /api/books/all** - ✅ All books without pagination working
  - [x] **GET /api/books/{id}** - ✅ Individual book retrieval working
  - [x] **POST /api/books** - ✅ Book creation working (returns HTTP 201)
  - [x] **PUT /api/books/{id}** - ✅ Book update working
  - [x] **DELETE /api/books/{id}** - ✅ Book deletion working
- [x] **Derived Queries**: Method-name based queries implemented and tested
- [x] **JPQL Queries**: Custom JPQL queries for complex operations
- [x] **Native Queries**: Raw SQL queries for database-specific features
- [x] **Criteria Queries**: Programmatic dynamic query building
- [x] **Real Data**: All endpoints return actual data from PostgreSQL
- [x] **Gateway Routing**: All endpoints accessible through Gateway (port 8090)
- [x] **Swagger UI**: ✅ Complete API documentation and testing interface available

## 🧪 SWAGGER UI TESTING EXAMPLES

### For testing in Swagger UI (`http://localhost:8090/swagger-ui.html`):

#### ✅ CREATE a new book (POST /api/books):
```json
{
  "title": "Mi Libro de Prueba",
  "author": "Autor de Ejemplo",
  "isbn": "9781111111111",
  "publicationDate": "2025-01-01",
  "pageCount": 200,
  "genre": "Prueba",
  "description": "Un libro creado desde Swagger UI",
  "availableCopies": 5,
  "totalCopies": 5
}
```

#### ✅ UPDATE an existing book (PUT /api/books/{id}):
```json
{
  "id": 1,
  "title": "El Señor de los Anillos - Edición Especial",
  "author": "J.R.R. Tolkien",
  "isbn": "9780544003415",
  "publicationDate": "1954-07-29",
  "pageCount": 1300,
  "genre": "Fantasía Épica",
  "description": "La obra maestra de Tolkien - Edición actualizada",
  "availableCopies": 10,
  "totalCopies": 10
}
```

#### ✅ READ operations:
- **GET /api/books?page=0&size=5** - Get first 5 books
- **GET /api/books/1** - Get specific book by ID
- **GET /api/books/all** - Get all books

#### ✅ DELETE operation:
- **DELETE /api/books/{id}** - Remove book by ID

## 🎯 REQUIREMENTS FULFILLED

✅ **Functional Database Connection**: The application successfully connects to PostgreSQL and loads real data

✅ **CRUD Endpoints Return Real Data**: All endpoints return actual data from the PostgreSQL database instead of mock data

✅ **Repository with Different Query Types**:
- **Native Query**: Direct SQL queries for statistics and complex operations
- **Derived Query**: Spring Data JPA method name conventions
- **Criteria Query**: Programmatic dynamic query building

✅ **PostgreSQL Docker Image**: Running PostgreSQL 15 in Docker with persistent data and initialization scripts

The implementation is complete and functional, providing a comprehensive example of all three query types with real database integration.