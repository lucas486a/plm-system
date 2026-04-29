package com.plm.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * DataSource configuration with HikariCP connection pool tuning.
 * Optimized for PLM system workload patterns:
 * - Moderate connection pool for typical concurrent users
 * - Optimized connection lifecycle for PostgreSQL
 * - Prepared statement caching for repeated queries
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        // Pool sizing - optimized for typical PLM workload
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);

        // Connection lifecycle
        dataSource.setConnectionTimeout(30000);    // 30s - max wait for connection
        dataSource.setIdleTimeout(600000);          // 10min - idle connection timeout
        dataSource.setMaxLifetime(1800000);          // 30min - max connection lifetime
        dataSource.setKeepaliveTime(300000);         // 5min - keepalive interval

        // Connection validation
        dataSource.setConnectionTestQuery("SELECT 1");

        // Performance optimizations
        dataSource.setLeakDetectionThreshold(60000); // 60s leak detection
        dataSource.setPoolName("PLM-HikariPool");

        // PostgreSQL-specific optimizations
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.addDataSourceProperty("useServerPrepStmts", "true");
        dataSource.addDataSourceProperty("reWriteBatchedInserts", "true");

        return dataSource;
    }
}
