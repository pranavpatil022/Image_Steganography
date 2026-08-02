package com.stego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class StegoApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(StegoApplication.class);
        Properties props = new Properties();
        // Ensure these limits are set regardless of property file loading issues
        props.setProperty("spring.servlet.multipart.max-file-size", "50MB");
        props.setProperty("spring.servlet.multipart.max-request-size", "50MB");
        app.setDefaultProperties(props);
        app.run(args);
    }
}
