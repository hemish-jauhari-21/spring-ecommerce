package com.demo.ecommerce.config;

import com.demo.ecommerce.exception.ErrorResponseUtil;
import com.demo.ecommerce.security.JwtAuthenticationEntryPoint;
import com.demo.ecommerce.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Public APIs
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/v1/ecommerce/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/ecommerce/products/**").permitAll()

                        // ADMIN only
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/ecommerce/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/ecommerce/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/ecommerce/products/**"
                        ).hasRole("ADMIN")

                        // User profile (own account) is for any authenticated user
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/ecommerce/user/me").authenticated()

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/ecommerce/user/me").authenticated()

                        // User management is ADMIN only
                        .requestMatchers(
                                "/api/v1/ecommerce/user/**"
                        ).hasRole("ADMIN")

                        // Global cart-item listing is ADMIN only
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/ecommerce/cart-item/all"
                        ).hasRole("ADMIN")

                        // Cross-user cart/order lookups are ADMIN only
                        .requestMatchers(
                                "/api/v1/ecommerce/cart/user/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v1/ecommerce/order/user/**"
                        ).hasRole("ADMIN")

                        // Order management is ADMIN only
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/ecommerce/order/all"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/ecommerce/order/*/status"
                        ).hasRole("ADMIN")

                        // Everything else requires login
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                ErrorResponseUtil.write(
                                        response,
                                        request,
                                        HttpStatus.FORBIDDEN,
                                        "You do not have permission to access this resource"
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}