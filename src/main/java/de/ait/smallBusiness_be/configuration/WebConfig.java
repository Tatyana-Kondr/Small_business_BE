package de.ait.smallBusiness_be.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // SPA fallback ONLY for non-API, non-static paths
        registry.addViewController("/{path:^(?!api|assets|media|uploads|favicon\\.ico$).*$}")
                .setViewName("forward:/index.html");

        registry.addViewController("/**/{path:^(?!api|assets|media|uploads).*$}")
                .setViewName("forward:/index.html");
    }
}