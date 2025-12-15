#!/bin/bash
# Build script to install local dependencies

set -e

echo "Installing local dependencies..."

# Install util dependency
cd ../util
mvn install -DskipTests

# Install dto-book dependency
cd ../dto-book
mvn install -DskipTests

# Now build ms-book
cd ../ms-book
docker build -t library/ms-book:latest .

echo "Build completed successfully!"