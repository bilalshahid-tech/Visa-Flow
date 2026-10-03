package com.visaflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DatabaseConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties properties) {
            String rawUrl = properties.getUrl();
            if (rawUrl != null && !rawUrl.isBlank()) {
                String cleanedUrl = sanitizeJdbcUrl(rawUrl);
                if (!cleanedUrl.equals(rawUrl)) {
                    log.info("Sanitized JDBC URL from '{}' to '{}'", rawUrl, cleanedUrl);
                    properties.setUrl(cleanedUrl);
                }
            }
        }
        return bean;
    }

    public static String sanitizeJdbcUrl(String url) {
        if (url == null) return null;
        String trimmed = url.trim();

        // Fix duplicated jdbc:postgresql:// prefixes
        while (trimmed.contains("jdbc:postgresql://jdbc:postgresql://")) {
            trimmed = trimmed.replace("jdbc:postgresql://jdbc:postgresql://", "jdbc:postgresql://");
        }

        // Fix leading ? if present (e.g. ?jdbc:postgresql://...)
        if (trimmed.startsWith("?jdbc:postgresql://")) {
            trimmed = trimmed.substring(1);
        }

        // Convert postgres:// or postgresql:// without jdbc: prefix
        if (trimmed.startsWith("postgres://")) {
            trimmed = "jdbc:postgresql://" + trimmed.substring("postgres://".length());
        } else if (trimmed.startsWith("postgresql://")) {
            trimmed = "jdbc:postgresql://" + trimmed.substring("postgresql://".length());
        }

        return trimmed;
    }
}
