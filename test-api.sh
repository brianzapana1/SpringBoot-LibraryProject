#!/bin/bash
# Comprehensive test script for the Book microservice

echo "============================================"
echo "       TESTING BOOK MICROSERVICE"
echo "============================================"

BASE_URL="http://localhost:8082"

echo ""
echo "1. Testing Health Check..."
curl -s "$BASE_URL/health" | jq '.'

echo ""
echo "2. Testing Info endpoint..."
curl -s "$BASE_URL/info"

echo ""
echo ""
echo "3. Testing DERIVED QUERY: Get all books (ordered by title)..."
curl -s "$BASE_URL/books/all" | jq '.[0:3]'

echo ""
echo ""
echo "4. Testing DERIVED QUERY: Search by author..."
curl -s "$BASE_URL/search/author?query=García" | jq '.'

echo ""
echo ""
echo "5. Testing DERIVED QUERY: Search by genre..."
curl -s "$BASE_URL/search/genre?genre=Ciencia%20Ficción" | jq '.'

echo ""
echo ""
echo "6. Testing JPQL QUERY: Get available books..."
curl -s "$BASE_URL/available" | jq '.[0:3]'

echo ""
echo ""
echo "7. Testing JPQL QUERY: Get unique authors..."
curl -s "$BASE_URL/authors" | jq '.[0:5]'

echo ""
echo ""
echo "8. Testing NATIVE QUERY: Book statistics by genre..."
curl -s "$BASE_URL/statistics/genre" | jq '.[0:5]'

echo ""
echo ""
echo "9. Testing NATIVE QUERY: Most borrowed books..."
curl -s "$BASE_URL/popular?limit=3" | jq '.'

echo ""
echo ""
echo "10. Testing NATIVE QUERY: Full text search..."
curl -s "$BASE_URL/search/fulltext?query=historia" | jq '.[0:2]'

echo ""
echo ""
echo "11. Testing DERIVED QUERY: Books by page range..."
curl -s "$BASE_URL/search/pages?minPages=200&maxPages=400" | jq '.[0:3]'

echo ""
echo ""
echo "12. Testing CRITERIA QUERY: Advanced search..."
curl -s "$BASE_URL/search/advanced?title=Don&author=Miguel&genre=Literatura%20Clásica" | jq '.'

echo ""
echo ""
echo "13. Testing CRITERIA QUERY: Advanced search with pagination..."
curl -s "$BASE_URL/search/advanced/paginated?searchTerm=ciencia&available=true&page=0&size=5&sortBy=title&sortDirection=ASC" | jq '.content[0:3]'

echo ""
echo ""
echo "14. Testing individual book retrieval..."
curl -s "$BASE_URL/books/1" | jq '.'

echo ""
echo ""
echo "============================================"
echo "       ALL TESTS COMPLETED"
echo "============================================"