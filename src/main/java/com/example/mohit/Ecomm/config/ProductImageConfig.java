package com.example.mohit.Ecomm.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProductImageConfig implements WebMvcConfigurer {

    private final String uploadDirectory;

    public ProductImageConfig(
            @Value("${app.upload.dir:uploads/products}")
            String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path imagePath = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        String resourceLocation = imagePath.toUri().toString();

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(resourceLocation);
    }
}