# Comprehensive test script for the Book microservice
Write-Host "============================================" -ForegroundColor Green
Write-Host "       TESTING BOOK MICROSERVICE" -ForegroundColor Green  
Write-Host "============================================" -ForegroundColor Green

$BaseUrl = "http://localhost:8082"

Write-Host "`n1. Testing Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/health" -Method Get
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. Testing Info endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/info" -Method Get
    Write-Host $response
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n3. Testing DERIVED QUERY: Get all books (ordered by title)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/books/all" -Method Get
    Write-Host "Total books found: $($response.Count)"
    $response[0..2] | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n4. Testing DERIVED QUERY: Search by author..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/search/author?query=García" -Method Get
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n5. Testing DERIVED QUERY: Search by genre..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/search/genre?genre=Ciencia Ficción" -Method Get
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n6. Testing JPQL QUERY: Get available books..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/available" -Method Get
    Write-Host "Available books found: $($response.Count)"
    $response[0..2] | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n7. Testing JPQL QUERY: Get unique authors..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/authors" -Method Get
    Write-Host "Authors found: $($response.Count)"
    $response[0..4]
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n8. Testing NATIVE QUERY: Book statistics by genre..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/statistics/genre" -Method Get
    $response[0..4] | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n9. Testing NATIVE QUERY: Most borrowed books..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/popular?limit=3" -Method Get
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n10. Testing NATIVE QUERY: Full text search..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/search/fulltext?query=historia" -Method Get
    $response[0..1] | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n11. Testing DERIVED QUERY: Books by page range..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/search/pages?minPages=200&maxPages=400" -Method Get
    $response[0..2] | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n12. Testing CRITERIA QUERY: Advanced search..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/search/advanced?title=Don&author=Miguel&genre=Literatura Clásica" -Method Get
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n13. Testing CRITERIA QUERY: Advanced search with pagination..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/search/advanced/paginated?searchTerm=ciencia&available=true&page=0&size=5&sortBy=title&sortDirection=ASC" -Method Get
    $response.content[0..2] | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n14. Testing individual book retrieval..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/books/1" -Method Get
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n============================================" -ForegroundColor Green
Write-Host "       ALL TESTS COMPLETED" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green