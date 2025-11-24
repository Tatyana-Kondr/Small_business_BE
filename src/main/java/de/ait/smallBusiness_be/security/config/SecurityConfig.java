package de.ait.smallBusiness_be.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
            "/swagger-ui.html",
            "/error"
    };

    private static final String[] OPEN_STATIC = {
            "/", "/index.html",
            "/assets/**",
            "/media/**",
            "/uploads/**",
            "/favicon.ico",
            "/vite.svg",
            "/manifest.webmanifest",
            "/robots.txt"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // включаем CORS, Spring будет использовать WebMvcConfigurer

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(OPEN_STATIC).permitAll()
                        .requestMatchers(AUTH_WHITELIST).permitAll() // Swagger доступен всем
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout", "/api/auth/refresh").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/companies", "/api/companies/{id}/logo", "/api/payment-methods", "/api/auth/register", "/api/product-categories" ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/users/{id}/role", "/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/companies/{id}", "/api/customers/{id}", "/api/payments/{id}", "/api/payment-methods/{id}", "/api/product-categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/{id}", "/api/payments/{id}", "/api/payment-methods/{id}", "/api/product-categories/{id}").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST,
                                "/api/customers",
                                "/api/payments",
                                "/api/payment-processes",
                                "/api/productions",
                                "/api/productionItems/{productionId}",
                                "/api/products",
                                "/api/units",
                                "/api/products/{productId}/files",
                                "/api/purchases",
                                "/api/purchaseItems/{purchaseId}",
                                "/api/document-types",
                                "/api/sales",
                                "/api/saleItems/{saleId}",
                                "/api/shippings").authenticated()
                        .requestMatchers(HttpMethod.PUT,
                                "/api/payment-processes/{id}",
                                "/api/productions/{id}",
                                "/api/productionItems/{id}",
                                "/api/products/{id}",
                                "/api/units/{id}",
                                "/api/purchases/{id}",
                                "/api/purchaseItems/{id}",
                                "/api/document-types/{id}",
                                "/api/sales/{id}",
                                "/api/saleItems/{saleId}/{saleItemId}",
                                "/api/shippings/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/payment-processes/{id}",
                                "/api/productions/{id}",
                                "/api/productionItems/{id}",
                                "/api/products/{id}",
                                "/api/units/{id}",
                                "/api/products/photos/{photoId}",
                                "/api/purchases/{id}",
                                "/api/purchaseItems/{id}",
                                "/api/document-types/{id}",
                                "/api/sales/{id}",
                                "/api/saleItems/{saleId}/{saleItemId}",
                                "/api/shippings/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/companies",
                                "/api/customers",
                                "/api/customers/customer-number",
                                "/api/customers/{id}",
                                "/api/payments",
                                "/api/payments/search/{query}",
                                "/api/payments/filter",
                                "/api/payments/{id}",
                                "/api/payments/prefill/sale/{saleId}",
                                "/api/payments/prefill/purchase/{purchaseId}",
                                "/api/payments/all-sale-ids",
                                "/api/payments/all-purchase-ids",
                                "/api/payment-methods",
                                "/api/payment-methods/{id}",
                                "/api/payment-processes",
                                "/api/payment-processes/{id}",
                                "/api/productions",
                                "/api/productions/{id}",
                                "/api/productions/search/**",
                                "/api/productions/filter",
                                "/api/productionItems/{id}",
                                "/api/products/{id}",
                                "/api/products",
                                "/api/products/category/{categoryId}",
                                "api/product-categories",
                                "api/product-categories/{id}",
                                "/api/units",
                                "/api/units/{id}",
                                "/api/products/{productId}/photos",
                                "/api/products/photos",
                                "/api/purchases",
                                "/api/purchases/{id}",
                                "/api/products/photos",
                                "/api/purchases/search/{query}",
                                "/api/purchases/filter",
                                "/api/purchaseItems/purchase/{purchaseId}",
                                "/api/purchaseItems/{id}",
                                "/api/document-types",
                                "/api/document-types/{id}",
                                "/api/sales",
                                "/api/sales/{id}",
                                "/api/sales/search/{query}",
                                "/api/sales/filter",
                                "/api/sales/invoices/{year}/{invoiceNumber}.pdf",
                                "/api/sales/delivery-bill/{year}/{deliveryBillNumber}.pdf",
                                "/api/saleItems/sale/{saleId}",
                                "/api/saleItems/{saleId}/{saleItemId}",
                                "/api/shippings",
                                "/api/shippings/{id}",
                                "/api/users/{id}",
                                "/api/auth/me",
                                "/api/warehouse/stocks",
                                "/api/warehouse/product/{productId}/history",
                                "/api/warehouse/product/{productId}/stock").authenticated()
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/purchases/{id}/update-payment-status",
                                "/api/sales/{id}/update-payment-status",
                                "/api/users/{id}/change-password").authenticated()
                        .anyRequest().permitAll()
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