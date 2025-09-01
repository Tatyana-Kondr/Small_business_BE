package de.ait.smallBusiness_be.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * SmallBusiness_BE
 * 19.11.2024
 *
 * @author Kondratyeva
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] AUTH_WHITELIST = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll() // Swagger доступен всем
                        .requestMatchers(HttpMethod.POST, "/api/users/register","/api/auth/login", "/api/auth/logout").permitAll()
//В                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/product-categories", "/api/customers", "/api/purchases", "/api/sales", "/api/payments").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/product-categories", "/api/purchases/{id}", "/api/productions/{id}", "/api/customers/{id}", "/api/sales/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/purchases/**").hasAuthority("ADMIN")
                         .requestMatchers(HttpMethod.DELETE, "/api/product-categories/{id}", "/api/products/{id}", "/api/purchaseItems/{id}", "/api/sales/{id}", "/api/productions/{id}", "/api/customers/{id}").hasAuthority("ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/product-categories/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/products", "/api/product-categories", "/api/customers").permitAll()
//                        .requestMatchers(HttpMethod.PUT, "/api/products/{id}", "/api/product-categories/{id}").permitAll()
//                        .requestMatchers(HttpMethod.DELETE, "/api/products/{id}",  "/api/product-categories/{id}").permitAll()
                        .anyRequest().authenticated()
                )

                // Stateless, потому что используем JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Добавляем JWT фильтр перед стандартным UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}