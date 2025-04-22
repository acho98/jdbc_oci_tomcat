#!/bin/bash

echo "DB Connection Tester Web Application"
echo "==================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in the PATH."
    echo "Please install Java and try again."
    exit 1
fi

# Start the Tomcat web server using Gradle
echo "Starting Tomcat web server..."
echo "The application will be available at http://localhost:8080"
echo "Press Ctrl+C to stop the server when done."
echo ""

# Try to run with Gradle wrapper first
if [ -f "./gradlew" ] && [ -f "./gradle/wrapper/gradle-wrapper.jar" ]; then
    # Make sure the wrapper is executable
    chmod +x ./gradlew
    
    # Run the web application using Gradle wrapper
    ./gradlew appRun
else
    # If wrapper fails or is not available, try with system Gradle
    if command -v gradle &> /dev/null; then
        echo "Using system Gradle instead of wrapper..."
        gradle appRun
    else
        echo "Error: Neither Gradle wrapper nor system Gradle is available."
        echo "Please install Gradle or fix the Gradle wrapper."
        exit 1
    fi
fi
