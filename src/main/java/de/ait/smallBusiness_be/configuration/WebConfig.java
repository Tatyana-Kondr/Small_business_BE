package de.ait.smallBusiness_be.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.root}")
    private String uploadRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Файлы, загруженные пользователями
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRoot + "/");

        // Фронтовая статика из JAR
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/media/**")
                .addResourceLocations("classpath:/static/media/");

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // SPA fallback (исключаем api, assets, media, uploads и все файлы с расширением)
        registry.addViewController("/{path:^(?!api|assets|media|uploads)(?!.*\\.[a-zA-Z0-9]+$).*$}")
                .setViewName("forward:/index.html");

        registry.addViewController("/**/{path:^(?!api|assets|media|uploads)(?!.*\\.[a-zA-Z0-9]+$).*$}")
                .setViewName("forward:/index.html");
    }
}
