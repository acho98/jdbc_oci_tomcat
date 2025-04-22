package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Servlet that handles database connection testing.
 * It accepts connection parameters from the request and attempts to connect to the database.
 */
public class DBConnectionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/plain");
        
        try {
            // Get connection parameters from request
            String host = getParameter(request, "host", "localhost");
            String port = getParameter(request, "port", "3306");
            String database = getParameter(request, "database", "mysql");
            String username = getParameter(request, "username", "root");
            String password = getParameter(request, "password", "");
            boolean useSSL = Boolean.parseBoolean(getParameter(request, "useSSL", "false"));
            String driverPath = getParameter(request, "driverPath", "");
            
            // Attempt to connect to the database
            String connectionResult = testDBConnection(host, port, database, username, password, useSSL, driverPath);
            
            // Send the result back to the client
            response.getWriter().write(connectionResult);
            
        } catch (Exception e) {
            response.getWriter().write("Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to get a parameter from the request with a default value if not present.
     */
    private String getParameter(HttpServletRequest request, String paramName, String defaultValue) {
        String value = request.getParameter(paramName);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
    
    /**
     * Tests the database connection with the provided parameters.
     */
    private String testDBConnection(String host, String port, String database, 
                                   String username, String password, boolean useSSL, String driverPath) {
        
        StringBuilder result = new StringBuilder();
        result.append("Attempting to connect to MySQL database:\n");
        result.append("Host: ").append(host).append("\n");
        result.append("Port: ").append(port).append("\n");
        result.append("Database: ").append(database).append("\n");
        result.append("Username: ").append(username).append("\n");
        result.append("Use SSL: ").append(useSSL).append("\n");
        
        if (!driverPath.isEmpty()) {
            result.append("JDBC Driver Path: ").append(driverPath).append("\n");
        } else {
            result.append("Using default JDBC driver\n");
        }
        result.append("\n");
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        
        try {
            // Load the MySQL JDBC driver
            if (!driverPath.isEmpty()) {
                // Use custom JDBC driver from provided path
                File driverFile = new File(driverPath);
                if (!driverFile.exists()) {
                    result.append("Error: JDBC driver file not found at specified path.\n");
                    result.append("Path: ").append(driverPath);
                    return result.toString();
                }
                
                // Create a URLClassLoader to load the driver from the specified path
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[] { driverFile.toURI().toURL() },
                    DBConnectionServlet.class.getClassLoader()
                );
                
                // Try to load the driver class
                try {
                    // First try MySQL 8.x driver class
                    Driver driver = (Driver) Class.forName("com.mysql.cj.jdbc.Driver", true, classLoader).newInstance();
                    DriverManager.registerDriver(new DriverWrapper(driver));
                    result.append("Loaded MySQL 8.x JDBC driver from: ").append(driverPath).append("\n\n");
                } catch (ClassNotFoundException e) {
                    try {
                        // If 8.x fails, try MySQL 5.x driver class
                        Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver", true, classLoader).newInstance();
                        DriverManager.registerDriver(new DriverWrapper(driver));
                        result.append("Loaded MySQL 5.x JDBC driver from: ").append(driverPath).append("\n\n");
                    } catch (ClassNotFoundException e2) {
                        result.append("Error: Could not find MySQL driver class in the specified JAR file.\n");
                        result.append("Please ensure the file is a valid MySQL JDBC driver.\n\n");
                        return result.toString();
                    }
                }
            } else {
                // Use default JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                result.append("Using default MySQL JDBC driver\n\n");
            }
            
            // Set connection properties
            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("useSSL", String.valueOf(useSSL));
            properties.setProperty("allowPublicKeyRetrieval", "true");
            
            // Attempt to establish a connection
            try (Connection conn = DriverManager.getConnection(url, properties)) {
                result.append("Connection successful!\n");
                result.append("Connected to: ").append(conn.getMetaData().getDatabaseProductName())
                      .append(" ").append(conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (ClassNotFoundException e) {
            result.append("Error: MySQL JDBC driver not found.\n");
            result.append("Error details: ").append(e.getMessage());
        } catch (SQLException e) {
            result.append("Connection failed!\n");
            result.append("Error code: ").append(e.getErrorCode()).append("\n");
            result.append("SQL State: ").append(e.getSQLState()).append("\n");
            result.append("Error message: ").append(e.getMessage());
        } catch (Exception e) {
            result.append("Error: ").append(e.getClass().getName()).append("\n");
            result.append("Error message: ").append(e.getMessage());
        }
        
        return result.toString();
    }
    
    /**
     * A wrapper class for the JDBC driver to handle class loader issues.
     */
    private static class DriverWrapper implements Driver {
        private final Driver driver;
        
        DriverWrapper(Driver driver) {
            this.driver = driver;
        }
        
        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return driver.connect(url, info);
        }
        
        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return driver.acceptsURL(url);
        }
        
        @Override
        public int getMajorVersion() {
            return driver.getMajorVersion();
        }
        
        @Override
        public int getMinorVersion() {
            return driver.getMinorVersion();
        }
        
        @Override
        public java.util.logging.Logger getParentLogger() {
            try {
                return driver.getParentLogger();
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return driver.getPropertyInfo(url, info);
        }
        
        @Override
        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }
    }
}
