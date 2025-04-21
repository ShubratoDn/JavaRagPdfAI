package com.java.rag.pdf;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {
    
    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
        
        // Set system properties for all env variables
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            System.setProperty(key, value);
        });
    }
}