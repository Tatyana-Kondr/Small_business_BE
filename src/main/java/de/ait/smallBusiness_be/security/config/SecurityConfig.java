package de.ait.smallBusiness_be.security.config;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SmallBusiness_BE
 * 19.11.2024
 *
 * @author Kondratyeva
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] AUTH_WHITELIST = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)

                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll() // Swagger доступен всем
                        .requestMatchers(HttpMethod.POST, "/api/users/register","/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "api/product-categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products", "api/product-categories", "api/customers").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}", "api/product-categories/{id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/products/{id}", "api/product-categories/{id}").permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(AbstractHttpConfigurer::disable) // Отключаем стандартный formLogin

                .logout(logout -> logout.logoutUrl("/api/auth/logout").logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                }))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Используем сессии
                );


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authProvider);
    }

    @Autowired
    public void bindUserDetailsServiceAndPasswordEncoder(UserDetailsService userDetailsServiceImpl,
                                                         PasswordEncoder passwordEncoder,
                                                         AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsServiceImpl)
                .passwordEncoder(passwordEncoder);
    }
}