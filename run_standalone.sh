#!/bin/bash

echo "MySQL DB Connection Tester - Standalone Mode"
echo "==========================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in the PATH."
    echo "Please install Java and try again."
    exit 1
fi

# Set variables
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WAR_FILE="$SCRIPT_DIR/mysql-db-connection-tester.war"
TOMCAT_DIR="$SCRIPT_DIR/embedded-tomcat"
TOMCAT_VERSION="10.1.15"
TOMCAT_URL="https://dlcdn.apache.org/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz"
TOMCAT_TAR="$SCRIPT_DIR/apache-tomcat-$TOMCAT_VERSION.tar.gz"
PORT=8080

# Check if WAR file exists
if [ ! -f "$WAR_FILE" ]; then
    echo "Error: WAR file not found at $WAR_FILE"
    echo "Please make sure the WAR file is in the same directory as this script."
    exit 1
fi

# Check if embedded Tomcat exists, if not download and extract it
if [ ! -d "$TOMCAT_DIR" ]; then
    echo "Embedded Tomcat not found. Downloading..."
    
    # Download Tomcat
    if command -v curl &> /dev/null; then
        curl -L "$TOMCAT_URL" -o "$TOMCAT_TAR"
    elif command -v wget &> /dev/null; then
        wget "$TOMCAT_URL" -O "$TOMCAT_TAR"
    else
        echo "Error: Neither curl nor wget is available to download Tomcat."
        echo "Please install either curl or wget and try again."
        exit 1
    fi
    
    # Extract Tomcat
    mkdir -p "$TOMCAT_DIR"
    tar -xzf "$TOMCAT_TAR" -C "$SCRIPT_DIR"
    mv "$SCRIPT_DIR/apache-tomcat-$TOMCAT_VERSION"/* "$TOMCAT_DIR/"
    rm -rf "$SCRIPT_DIR/apache-tomcat-$TOMCAT_VERSION"
    rm "$TOMCAT_TAR"
    
    # Make scripts executable
    chmod +x "$TOMCAT_DIR/bin/"*.sh
    
    echo "Embedded Tomcat downloaded and extracted."
fi

# Deploy the WAR file to Tomcat
echo "Deploying application to Tomcat..."
rm -rf "$TOMCAT_DIR/webapps/ROOT"
cp "$WAR_FILE" "$TOMCAT_DIR/webapps/ROOT.war"

# Start Tomcat
echo "Starting Tomcat server..."
echo "The application will be available at http://localhost:$PORT"
echo "Press Ctrl+C to stop the server when done."
echo ""

# Run Tomcat
"$TOMCAT_DIR/bin/catalina.sh" run
