#!/bin/bash

echo "Packaging MySQL DB Connection Tester Web Application..."

# Create dist directory if it doesn't exist
mkdir -p dist

# Build the WAR file using Gradle
echo "Building WAR file..."
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew clean war
else
    echo "Gradle wrapper not found. Trying with system Gradle..."
    gradle clean war
fi

# Check if the build was successful
if [ ! -f "build/libs/"*.war ]; then
    echo "Failed to build WAR file. Check for errors above."
    exit 1
fi

# Copy the WAR file to the dist directory
WAR_FILE=$(find build/libs -name "*.war" | head -n 1)
WAR_FILENAME=$(basename "$WAR_FILE")
cp "$WAR_FILE" "dist/$WAR_FILENAME"

# Create a package with all necessary files
TEMP_DIR="temp_package"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

# Copy files
cp -r build/libs/*.war "$TEMP_DIR/"
cp run_db_tester.sh "$TEMP_DIR/"
cp README.md "$TEMP_DIR/"
cp -r lib "$TEMP_DIR/" 2>/dev/null || mkdir -p "$TEMP_DIR/lib"

# Make shell script executable
chmod +x "$TEMP_DIR/run_db_tester.sh"

# Create ZIP file
ZIP_FILE="dist/mysql-db-connection-tester-web.zip"
rm -f "$ZIP_FILE"

if command -v zip &> /dev/null; then
    (cd "$TEMP_DIR" && zip -r "../$ZIP_FILE" .)
    echo "Package created: $ZIP_FILE"
else
    echo "Warning: 'zip' command not found. Package not created."
    echo "You can manually zip the contents of the $TEMP_DIR directory."
fi

# Clean up
echo "Cleaning up temporary files..."
rm -rf "$TEMP_DIR"

echo "Packaging complete!"
echo "The WAR file is available at: dist/$WAR_FILENAME"
echo "The complete package is available at: $ZIP_FILE"
