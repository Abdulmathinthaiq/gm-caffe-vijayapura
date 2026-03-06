package com.gmcaffe.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.StringUtils;

/**
 * Enhanced debug logging configuration for MySQL connections.
 * This configuration provides detailed logging for:
 * - Database connection URLs (sanitized for security)
 * - Driver information
 * - Connection pool settings
 * 
 * The actual DataSource is auto-configured by Spring Boot with HikariCP.
 * Debug logging is enabled via application.properties settings.
 */
public class MySQLDebugConfig {

    private static final Logger logger = LoggerFactory.getLogger(MySQLDebugConfig.class);

    @Autowired
    private DataSourceProperties dataSourceProperties;

    /**
     * Logs the datasource configuration details at startup.
     * This method should be called from a @PostConstruct or CommandLineRunner.
     */
    public void logDataSourceConfiguration() {
        logger.info("========================================");
        logger.info("MySQL Connection Debug Configuration");
        logger.info("========================================");
        
        // Log driver class
        String driverClassName = dataSourceProperties.getDriverClassName();
        logger.info("Driver Class: {}", driverClassName);
        
        // Log and sanitize URL
        String url = dataSourceProperties.getUrl();
        if (StringUtils.hasText(url)) {
            String sanitizedUrl = sanitizeUrl(url);
            logger.info("Database URL: {}", sanitizedUrl);
            
            // Extract and log database type
            if (url.contains("mysql")) {
                logger.info("Database Type: MySQL");
            } else if (url.contains("h2")) {
                logger.info("Database Type: H2");
            }
            
            // Log URL parameters for debugging
            if (url.contains("?")) {
                String params = url.substring(url.indexOf("?") + 1);
                logger.debug("URL Parameters: {}", params);
            }
        }
        
        // Log username (don't log password)
        String username = dataSourceProperties.getUsername();
        if (StringUtils.hasText(username)) {
            logger.info("Database Username: {}", username);
        }
        
        logger.info("========================================");
        logger.info("For detailed MySQL connection debug logs,");
        logger.info("check the following log levels:");
        logger.info("  - com.mysql.cj (MySQL driver)");
        logger.info("  - com.zaxero.hikari.pool (HikariCP pool)");
        logger.info("  - org.hibernate.SQL (SQL queries)");
        logger.info("========================================");
    }

    /**
     * Sanitizes the database URL by masking sensitive information.
     */
    public String sanitizeUrl(String url) {
        if (url == null) {
            return "null";
        }
        
        // Mask password in URL if present
        if (url.contains("@")) {
            // Format: jdbc:mysql://user:password@host:port/db
            return url.replaceAll("(:[^:@]+)@", ":****@");
        }
        
        return url;
    }
}
