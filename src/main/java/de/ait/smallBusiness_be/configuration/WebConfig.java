package de.ait.smallBusiness_be.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Метод возвращает каталог, где лежит jar-файл
    private static String getJarDir() {
        try {
            return Paths.get(WebConfig.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent()
                    .toString();
        } catch (Exception e) {
            // fallback — на случай, если jar не удаётся определить
            return System.getProperty("user.dir");
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = getJarDir() + "/uploads/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + baseDir)
                .setCachePeriod(3600); // 1 час кэш
    }
}