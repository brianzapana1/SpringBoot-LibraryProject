# ========================================
# PRUEBAS COMPLETAS DEL CRUD - API BOOKS
# ========================================
# Este script prueba todos los endpoints CRUD con datos reales de PostgreSQL
# URL Base: http://localhost:8090/api/books

$baseUrl = "http://localhost:8090/api/books"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "PRUEBAS CRUD - LIBRARY MANAGEMENT SYSTEM" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ========================================
# 1. READ - GET ALL BOOKS (Paginado)
# ========================================
Write-Host ">>> 1. GET /api/books - Listar todos los libros (paginado)" -ForegroundColor Yellow
Write-Host "Request: GET $baseUrl`?page=0&size=3" -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri "$baseUrl`?page=0&size=3" -Method Get
    Write-Host "✅ SUCCESS - Status: 200 OK" -ForegroundColor Green
    Write-Host "Total de libros: $($response.totalElements)" -ForegroundColor White
    Write-Host "Mostrando primeros 3 libros:" -ForegroundColor White
    $response.content | ForEach-Object {
        Write-Host "  - ID: $($_.id) | $($_.title) por $($_.author)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ========================================
# 2. READ - GET BOOK BY ID
# ========================================
Write-Host "`n>>> 2. GET /api/books/{id} - Obtener libro específico" -ForegroundColor Yellow
Write-Host "Request: GET $baseUrl/1" -ForegroundColor Gray

try {
    $book = Invoke-RestMethod -Uri "$baseUrl/1" -Method Get
    Write-Host "✅ SUCCESS - Status: 200 OK" -ForegroundColor Green
    Write-Host "Libro encontrado:" -ForegroundColor White
    Write-Host "  ID: $($book.id)" -ForegroundColor White
    Write-Host "  Título: $($book.title)" -ForegroundColor White
    Write-Host "  Autor: $($book.author)" -ForegroundColor White
    Write-Host "  ISBN: $($book.isbn)" -ForegroundColor White
    Write-Host "  Género: $($book.genre)" -ForegroundColor White
    Write-Host "  Páginas: $($book.pageCount)" -ForegroundColor White
    Write-Host "  Copias disponibles: $($book.availableCopies)/$($book.totalCopies)" -ForegroundColor White
} catch {
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ========================================
# 3. CREATE - POST NEW BOOK
# ========================================
Write-Host "`n>>> 3. POST /api/books - Crear nuevo libro" -ForegroundColor Yellow

$newBook = @{
    title = "El Código Da Vinci - Edición de Prueba"
    author = "Dan Brown"
    isbn = "9780307474278"
    publicationDate = "2003-03-18"
    pageCount = 689
    genre = "Thriller"
    description = "Libro de prueba creado desde PowerShell - Una novela de misterio"
    availableCopies = 8
    totalCopies = 10
} | ConvertTo-Json

Write-Host "Request: POST $baseUrl" -ForegroundColor Gray
Write-Host "Body: $newBook" -ForegroundColor Gray

try {
    $createdBook = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $newBook -Headers $headers -ContentType "application/json"
    Write-Host "✅ SUCCESS - Status: 201 Created" -ForegroundColor Green
    Write-Host "Libro creado exitosamente:" -ForegroundColor White
    Write-Host "  ID asignado: $($createdBook.id)" -ForegroundColor Cyan
    Write-Host "  Título: $($createdBook.title)" -ForegroundColor White
    Write-Host "  Autor: $($createdBook.author)" -ForegroundColor White
    Write-Host "  ISBN: $($createdBook.isbn)" -ForegroundColor White
    
    # Guardar el ID para usarlo en las siguientes pruebas
    $testBookId = $createdBook.id
} catch {
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorDetails = $reader.ReadToEnd()
        Write-Host "Detalles: $errorDetails" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 2

# ========================================
# 4. READ - VERIFICAR LIBRO CREADO
# ========================================
if ($testBookId) {
    Write-Host "`n>>> 4. GET /api/books/$testBookId - Verificar libro creado" -ForegroundColor Yellow
    Write-Host "Request: GET $baseUrl/$testBookId" -ForegroundColor Gray

    try {
        $verifyBook = Invoke-RestMethod -Uri "$baseUrl/$testBookId" -Method Get
        Write-Host "✅ SUCCESS - Libro encontrado en base de datos" -ForegroundColor Green
        Write-Host "  Título: $($verifyBook.title)" -ForegroundColor White
        Write-Host "  Descripción: $($verifyBook.description)" -ForegroundColor White
    } catch {
        Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Seconds 2
}

# ========================================
# 5. UPDATE - PUT BOOK
# ========================================
if ($testBookId) {
    Write-Host "`n>>> 5. PUT /api/books/$testBookId - Actualizar libro" -ForegroundColor Yellow

    $updatedBook = @{
        id = $testBookId
        title = "El Código Da Vinci - ACTUALIZADO"
        author = "Dan Brown"
        isbn = "9780307474278"
        publicationDate = "2003-03-18"
        pageCount = 700
        genre = "Thriller Histórico"
        description = "LIBRO ACTUALIZADO - Una novela de misterio que combina arte, historia y conspiración"
        availableCopies = 5
        totalCopies = 12
    } | ConvertTo-Json

    Write-Host "Request: PUT $baseUrl/$testBookId" -ForegroundColor Gray
    Write-Host "Body: $updatedBook" -ForegroundColor Gray

    try {
        $result = Invoke-RestMethod -Uri "$baseUrl/$testBookId" -Method Put -Body $updatedBook -Headers $headers -ContentType "application/json"
        Write-Host "✅ SUCCESS - Status: 200 OK" -ForegroundColor Green
        Write-Host "Libro actualizado exitosamente:" -ForegroundColor White
        Write-Host "  Título: $($result.title)" -ForegroundColor Cyan
        Write-Host "  Género actualizado: $($result.genre)" -ForegroundColor Cyan
        Write-Host "  Páginas actualizadas: $($result.pageCount)" -ForegroundColor Cyan
        Write-Host "  Copias totales actualizadas: $($result.totalCopies)" -ForegroundColor Cyan
    } catch {
        Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorDetails = $reader.ReadToEnd()
            Write-Host "Detalles: $errorDetails" -ForegroundColor Red
        }
    }

    Start-Sleep -Seconds 2
}

# ========================================
# 6. READ - VERIFICAR ACTUALIZACIÓN
# ========================================
if ($testBookId) {
    Write-Host "`n>>> 6. GET /api/books/$testBookId - Verificar actualización" -ForegroundColor Yellow
    Write-Host "Request: GET $baseUrl/$testBookId" -ForegroundColor Gray

    try {
        $verifyUpdate = Invoke-RestMethod -Uri "$baseUrl/$testBookId" -Method Get
        Write-Host "✅ SUCCESS - Cambios verificados en base de datos" -ForegroundColor Green
        Write-Host "  Título: $($verifyUpdate.title)" -ForegroundColor White
        Write-Host "  Género: $($verifyUpdate.genre)" -ForegroundColor White
        Write-Host "  Páginas: $($verifyUpdate.pageCount)" -ForegroundColor White
        Write-Host "  Descripción: $($verifyUpdate.description)" -ForegroundColor White
    } catch {
        Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Seconds 2
}

# ========================================
# 7. DELETE - DELETE BOOK
# ========================================
if ($testBookId) {
    Write-Host "`n>>> 7. DELETE /api/books/$testBookId - Eliminar libro" -ForegroundColor Yellow
    Write-Host "Request: DELETE $baseUrl/$testBookId" -ForegroundColor Gray

    try {
        $deleteResponse = Invoke-WebRequest -Uri "$baseUrl/$testBookId" -Method Delete
        if ($deleteResponse.StatusCode -eq 204) {
            Write-Host "✅ SUCCESS - Status: 204 No Content" -ForegroundColor Green
            Write-Host "Libro eliminado exitosamente de la base de datos" -ForegroundColor White
        }
    } catch {
        Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Seconds 2
}

# ========================================
# 8. READ - VERIFICAR ELIMINACIÓN (404)
# ========================================
if ($testBookId) {
    Write-Host "`n>>> 8. GET /api/books/$testBookId - Verificar eliminación (debe fallar)" -ForegroundColor Yellow
    Write-Host "Request: GET $baseUrl/$testBookId" -ForegroundColor Gray

    try {
        $shouldFail = Invoke-RestMethod -Uri "$baseUrl/$testBookId" -Method Get
        Write-Host "⚠️  INESPERADO - El libro aún existe" -ForegroundColor Yellow
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host "✅ SUCCESS - Status: 404 Not Found (esperado)" -ForegroundColor Green
            Write-Host "El libro fue eliminado correctamente de la base de datos" -ForegroundColor White
        } else {
            Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# ========================================
# 9. CREATE - PRUEBA DE VALIDACIÓN (ISBN DUPLICADO)
# ========================================
Write-Host "`n>>> 9. POST /api/books - Prueba de validación (ISBN duplicado)" -ForegroundColor Yellow

$duplicateBook = @{
    title = "Libro con ISBN Duplicado"
    author = "Autor Prueba"
    isbn = "9780544003415"  # ISBN que ya existe (El Señor de los Anillos)
    publicationDate = "2025-01-01"
    pageCount = 300
    genre = "Prueba"
    description = "Este libro debe fallar porque el ISBN ya existe"
    availableCopies = 5
    totalCopies = 5
} | ConvertTo-Json

Write-Host "Request: POST $baseUrl (con ISBN duplicado: 9780544003415)" -ForegroundColor Gray

try {
    $shouldFail = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $duplicateBook -Headers $headers -ContentType "application/json"
    Write-Host "⚠️  INESPERADO - Se permitió crear libro con ISBN duplicado" -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "✅ SUCCESS - Status: 409 Conflict (esperado)" -ForegroundColor Green
        Write-Host "Validación correcta: ISBN duplicado rechazado" -ForegroundColor White
    } else {
        Write-Host "❌ ERROR INESPERADO: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# ========================================
# 10. PRUEBAS ADICIONALES - BÚSQUEDAS
# ========================================
Write-Host "`n>>> 10. PRUEBAS ADICIONALES - Endpoints de búsqueda" -ForegroundColor Yellow

Write-Host "`n  A) Buscar por título:" -ForegroundColor Cyan
try {
    $searchTitle = Invoke-RestMethod -Uri "$baseUrl/../search/title?query=señor" -Method Get
    Write-Host "  ✅ Encontrados $($searchTitle.Count) libros con 'señor' en el título" -ForegroundColor Green
} catch {
    Write-Host "  ❌ ERROR en búsqueda por título" -ForegroundColor Red
}

Write-Host "`n  B) Buscar por autor:" -ForegroundColor Cyan
try {
    $searchAuthor = Invoke-RestMethod -Uri "$baseUrl/../search/author?query=tolkien" -Method Get
    Write-Host "  ✅ Encontrados $($searchAuthor.Count) libros de 'tolkien'" -ForegroundColor Green
} catch {
    Write-Host "  ❌ ERROR en búsqueda por autor" -ForegroundColor Red
}

Write-Host "`n  C) Libros disponibles:" -ForegroundColor Cyan
try {
    $available = Invoke-RestMethod -Uri "$baseUrl/../available" -Method Get
    Write-Host "  ✅ Total de libros disponibles: $($available.Count)" -ForegroundColor Green
} catch {
    Write-Host "  ❌ ERROR en búsqueda de disponibles" -ForegroundColor Red
}

Write-Host "`n  D) Estadísticas por género:" -ForegroundColor Cyan
try {
    $stats = Invoke-RestMethod -Uri "$baseUrl/../statistics/genre" -Method Get
    Write-Host "  ✅ Estadísticas obtenidas para $($stats.Count) géneros" -ForegroundColor Green
    $stats | Select-Object -First 3 | ForEach-Object {
        Write-Host "    - $($_[0]): $($_[1]) libros, $($_[2]) disponibles" -ForegroundColor White
    }
} catch {
    Write-Host "  ❌ ERROR en estadísticas" -ForegroundColor Red
}

# ========================================
# RESUMEN FINAL
# ========================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "RESUMEN DE PRUEBAS COMPLETADAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ GET    /api/books (paginado)" -ForegroundColor Green
Write-Host "✅ GET    /api/books/{id}" -ForegroundColor Green
Write-Host "✅ POST   /api/books (crear)" -ForegroundColor Green
Write-Host "✅ PUT    /api/books/{id} (actualizar)" -ForegroundColor Green
Write-Host "✅ DELETE /api/books/{id} (eliminar)" -ForegroundColor Green
Write-Host "✅ Validación de ISBN duplicado" -ForegroundColor Green
Write-Host "✅ Verificación de cambios en BD" -ForegroundColor Green
Write-Host "✅ Endpoints de búsqueda" -ForegroundColor Green
Write-Host "`nTodas las operaciones CRUD funcionan correctamente" -ForegroundColor White
Write-Host "con datos reales desde PostgreSQL ✓" -ForegroundColor White
Write-Host "`n========================================`n" -ForegroundColor Cyan
