#!/bin/bash

echo "DB Connection Tester Web Application"
echo "==================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in the PATH."
    echo "Please install Java and try again."
    exit 1
fi

# Check if Gradle is installed
if ! command -v ./gradlew &> /dev/null; then
    echo "Error: Gradle wrapper not found."
    echo "Please ensure you're in the correct directory."
    exit 1
fi

# Start the Tomcat web server using Gradle
echo "Starting Tomcat web server..."
echo "The application will be available at http://localhost:8080"
echo "Press Ctrl+C to stop the server when done."
echo ""

# Run the web application using Gradle
./gradlew appRun
