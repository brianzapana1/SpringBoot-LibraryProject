# Keycloak Authentication Test Script
Write-Host "=== Testing Keycloak Authentication ===" -ForegroundColor Green

$KEYCLOAK_URL = "http://localhost:8180"
$REALM_NAME = "library-realm"
$CLIENT_ID = "library-client"
$CLIENT_SECRET = "library-client-secret"
$BOOK_SERVICE_URL = "http://localhost:8001"

Write-Host "`n1. Testing Admin User Authentication..." -ForegroundColor Yellow

# Get token for admin user
$adminTokenBody = @{
    username = "libraryadmin"
    password = "admin123"
    grant_type = "password"
    client_id = $CLIENT_ID
    client_secret = $CLIENT_SECRET
}

try {
    $adminTokenResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/$REALM_NAME/protocol/openid-connect/token" -Method Post -Body $adminTokenBody -ContentType "application/x-www-form-urlencoded"
    $ADMIN_ACCESS_TOKEN = $adminTokenResponse.access_token
    Write-Host "✅ Admin token obtained successfully" -ForegroundColor Green
    
    # Test admin endpoint (create book)
    $bookData = @{
        title = "Test Book for Admin"
        author = "Test Author"
        isbn = "978-0123456789"
        genre = "Technology"
        publicationDate = "2024-01-01"
        publisher = "Test Publisher"
        pageCount = 300
        description = "Test book created by admin user"
        availableCopies = 5
    } | ConvertTo-Json

    $adminHeaders = @{
        "Authorization" = "Bearer $ADMIN_ACCESS_TOKEN"
        "Content-Type" = "application/json"
    }

    try {
        $createResponse = Invoke-RestMethod -Uri "$BOOK_SERVICE_URL/ms-book/v1/api/books" -Method Post -Body $bookData -Headers $adminHeaders
        Write-Host "✅ Admin successfully created book: $($createResponse.title)" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Failed to create book as admin: $($_.Exception.Message)" -ForegroundColor Red
    }

}
catch {
    Write-Host "❌ Failed to get admin token: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. Testing Regular User Authentication..." -ForegroundColor Yellow

# Get token for regular user
$userTokenBody = @{
    username = "libraryuser"
    password = "user123"
    grant_type = "password"
    client_id = $CLIENT_ID
    client_secret = $CLIENT_SECRET
}

try {
    $userTokenResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/$REALM_NAME/protocol/openid-connect/token" -Method Post -Body $userTokenBody -ContentType "application/x-www-form-urlencoded"
    $USER_ACCESS_TOKEN = $userTokenResponse.access_token
    Write-Host "✅ User token obtained successfully" -ForegroundColor Green
    
    # Test user endpoint (read books)
    $userHeaders = @{
        "Authorization" = "Bearer $USER_ACCESS_TOKEN"
        "Content-Type" = "application/json"
    }

    try {
        $booksResponse = Invoke-RestMethod -Uri "$BOOK_SERVICE_URL/ms-book/v1/api/books" -Method Get -Headers $userHeaders
        Write-Host "✅ User successfully retrieved books: $($booksResponse.totalElements) total books" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Failed to retrieve books as user: $($_.Exception.Message)" -ForegroundColor Red
    }

    # Test user trying to create book (should fail)
    Write-Host "`n3. Testing Authorization (User trying admin operation)..." -ForegroundColor Yellow
    try {
        $unauthorizedBook = @{
            title = "Unauthorized Book"
            author = "Unauthorized Author"
            isbn = "978-9999999999"
            genre = "Unauthorized"
            publicationDate = "2024-01-01"
            publisher = "Unauthorized Publisher"
            pageCount = 100
            description = "This should fail"
            availableCopies = 1
        } | ConvertTo-Json

        Invoke-RestMethod -Uri "$BOOK_SERVICE_URL/ms-book/v1/api/books" -Method Post -Body $unauthorizedBook -Headers $userHeaders -ErrorAction Stop
        Write-Host "❌ User was able to create book (this should have failed!)" -ForegroundColor Red
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 403) {
            Write-Host "✅ User correctly denied access to create book (403 Forbidden)" -ForegroundColor Green
        } else {
            Write-Host "❌ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
        }
    }

}
catch {
    Write-Host "❌ Failed to get user token: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n4. Testing Unauthorized Access..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$BOOK_SERVICE_URL/ms-book/v1/api/books" -Method Get -ErrorAction Stop
    Write-Host "❌ Unauthenticated request succeeded (this should have failed!)" -ForegroundColor Red
}
catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Unauthenticated request correctly denied (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "❌ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Authentication Test Complete ===" -ForegroundColor Green
Write-Host "`nTo manually test tokens:" -ForegroundColor Cyan
Write-Host "Admin Token: $ADMIN_ACCESS_TOKEN" -ForegroundColor Gray
Write-Host "User Token: $USER_ACCESS_TOKEN" -ForegroundColor Gray
Write-Host "`nExample curl commands:" -ForegroundColor Cyan
Write-Host "curl -H `"Authorization: Bearer \$ADMIN_TOKEN`" -X GET $BOOK_SERVICE_URL/ms-book/v1/api/books" -ForegroundColor Gray
Write-Host "curl -H `"Authorization: Bearer \$USER_TOKEN`" -X GET $BOOK_SERVICE_URL/ms-book/v1/api/books" -ForegroundColor Gray