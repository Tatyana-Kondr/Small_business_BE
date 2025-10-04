package de.ait.smallBusiness_be.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/photos/");
        registry.addResourceHandler("/uploads/logos/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/logos/");
        registry.addResourceHandler("/uploads/documents/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/documents/");
    }
}