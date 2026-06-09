package com.Ai.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class VectorStoreConfig {

    @Value("${pgvector.datasource.url}")
    private String pgUrl;

    @Value("${pgvector.datasource.username}")
    private String pgUsername;

    @Value("${pgvector.datasource.password}")
    private String pgPassword;

    @Value("${pgvector.datasource.driver-class-name}")
    private String pgDriverClassName;

    @Bean
    public DataSource pgVectorDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(pgUrl);
        dataSource.setUsername(pgUsername);
        dataSource.setPassword(pgPassword);
        dataSource.setDriverClassName(pgDriverClassName);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        return dataSource;
    }

    @Bean
    public PgVectorStore pgVectorStore(DataSource pgVectorDataSource) {
        return PgVectorStore.builder(pgVectorDataSource)
                .initializeSchema(true)
                .build();
    }
}
