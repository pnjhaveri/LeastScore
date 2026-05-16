package com.example.leastscore.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataSourceConfig {

    @Bean
    @Profile("production")
    public DataSource dataSource(DataSourceProperties properties) {
        var url = System.getenv("DATABASE_URL");
        if (url != null && !url.isBlank() && !url.startsWith("jdbc")) {
            var jdbcUrl = url
                .replaceFirst("^postgres(ql)?://", "jdbc:postgresql://")
                .replaceFirst("\\?(.*)$", "&$1");
            var builder = DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(extractUser(url))
                .password(extractPassword(url));
            String poolSize = System.getenv("DB_POOL_SIZE");
            if (poolSize != null) {
                builder = builder.driverClassName("org.postgresql.Driver");
            }
            return builder.build();
        }
        return properties.initializeDataSourceBuilder().build();
    }

    private static String extractUser(String url) {
        var withoutScheme = url.replaceFirst("^postgres(ql)?://", "");
        return withoutScheme.substring(0, withoutScheme.indexOf(':'));
    }

    private static String extractPassword(String url) {
        var withoutScheme = url.replaceFirst("^postgres(ql)?://", "");
        var atIndex = withoutScheme.indexOf('@');
        return withoutScheme.substring(withoutScheme.indexOf(':') + 1, atIndex);
    }
}
