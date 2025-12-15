-- Create database for Keycloak if running as superuser
-- This will be ignored if the database already exists
CREATE DATABASE keycloak_db;

-- Crear la tabla books si no existe
CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    publication_date DATE,
    page_count INTEGER,
    genre VARCHAR(100),
    description TEXT,
    available_copies INTEGER DEFAULT 0,
    total_copies INTEGER DEFAULT 0
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_genre ON books(genre);
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_available ON books(available_copies);

-- Insertar datos de ejemplo
INSERT INTO books (title, author, isbn, publication_date, page_count, genre, description, available_copies, total_copies) VALUES
-- Literatura Clásica
('Cien años de soledad', 'Gabriel García Márquez', '978-84-376-0494-7', '1967-06-05', 471, 'Realismo Mágico', 'Una obra maestra de la literatura latinoamericana que narra la saga de la familia Buendía.', 3, 5),
('Don Quijote de la Mancha', 'Miguel de Cervantes', '978-84-376-0495-8', '1605-01-16', 863, 'Literatura Clásica', 'La historia del ingenioso hidalgo Don Quijote de la Mancha y sus aventuras.', 2, 4),
('1984', 'George Orwell', '978-0-452-28423-4', '1949-06-08', 328, 'Distopía', 'Una novela distópica sobre un futuro totalitario donde el Gran Hermano lo vigila todo.', 4, 6),

-- Ciencia Ficción
('Dune', 'Frank Herbert', '978-0-441-17271-9', '1965-08-01', 688, 'Ciencia Ficción', 'Una épica novela de ciencia ficción ambientada en el planeta desértico Arrakis.', 2, 3),
('Neuromante', 'William Gibson', '978-0-441-56956-9', '1984-07-01', 271, 'Cyberpunk', 'La novela que definió el género cyberpunk y popularizó el concepto de ciberespacio.', 1, 2),
('El fin de la eternidad', 'Isaac Asimov', '978-0-553-29579-3', '1955-09-01', 191, 'Ciencia Ficción', 'Una historia sobre viajes en el tiempo y la manipulación de la historia humana.', 3, 4),

-- Fantasía
('El Señor de los Anillos: La Comunidad del Anillo', 'J.R.R. Tolkien', '978-0-547-92822-7', '1954-07-29', 479, 'Fantasía', 'La primera parte de la épica trilogía de la Tierra Media.', 5, 8),
('Juego de Tronos', 'George R.R. Martin', '978-0-553-10354-0', '1996-08-01', 694, 'Fantasía Épica', 'El primer libro de la serie Canción de Hielo y Fuego, llena de intriga política.', 3, 5),
('El Nombre del Viento', 'Patrick Rothfuss', '978-0-7564-0407-9', '2007-03-27', 662, 'Fantasía', 'La historia de Kvothe, un joven que se convierte en leyenda.', 2, 3),

-- Ciencias
('Una breve historia del tiempo', 'Stephen Hawking', '978-0-553-38016-3', '1988-04-01', 212, 'Ciencia', 'Una exploración accesible de los misterios del universo y la física moderna.', 4, 6),
('El origen de las especies', 'Charles Darwin', '978-0-14-043205-1', '1859-11-24', 703, 'Ciencia', 'La obra fundamental que introdujo la teoría de la evolución por selección natural.', 2, 3),
('Cosmos', 'Carl Sagan', '978-0-345-33135-9', '1980-09-28', 365, 'Astronomía', 'Un viaje fascinante a través del cosmos y nuestro lugar en el universo.', 3, 4),

-- Programación y Tecnología
('Clean Code', 'Robert C. Martin', '978-0-13-235088-4', '2008-08-11', 464, 'Programación', 'Principios y prácticas para escribir código limpio y mantenible.', 5, 7),
('Design Patterns', 'Gang of Four', '978-0-201-63361-0', '1994-10-21', 395, 'Programación', 'Los patrones de diseño fundamentales en la programación orientada a objetos.', 3, 5),
('Effective Java', 'Joshua Bloch', '978-0-13-468599-1', '2017-12-27', 412, 'Java', 'Las mejores prácticas para programar en Java de manera efectiva.', 4, 6),

-- Historia
('Sapiens: De animales a dioses', 'Yuval Noah Harari', '978-0-06-231609-7', '2014-02-10', 443, 'Historia', 'Una mirada fascinante a la historia de la humanidad desde sus orígenes.', 6, 8),
('El Arte de la Guerra', 'Sun Tzu', '978-1-59030-963-7', '500-01-01', 273, 'Estrategia', 'El clásico tratado militar chino sobre estrategia y táctica.', 2, 4),

-- Filosofía
('Así habló Zaratustra', 'Friedrich Nietzsche', '978-0-14-044118-3', '1883-01-01', 352, 'Filosofía', 'Una obra filosófica que explora temas de moral, religión y existencia.', 2, 3),
('El mundo de Sofia', 'Jostein Gaarder', '978-84-7888-841-5', '1991-01-01', 638, 'Filosofía', 'Una introducción accesible a la historia de la filosofía a través de una novela.', 4, 5),

-- Desarrollo Personal
('Los 7 hábitos de la gente altamente efectiva', 'Stephen Covey', '978-1-982-13788-6', '1989-08-15', 372, 'Autoayuda', 'Principios fundamentales para el desarrollo personal y profesional.', 5, 7),
('Atomic Habits', 'James Clear', '978-0-7352-1129-2', '2018-10-16', 320, 'Autoayuda', 'Estrategias prácticas para formar buenos hábitos y romper los malos.', 3, 6);

-- Verificar que los datos se insertaron correctamente
SELECT COUNT(*) as total_books FROM books;
SELECT genre, COUNT(*) as books_per_genre FROM books GROUP BY genre ORDER BY books_per_genre DESC;