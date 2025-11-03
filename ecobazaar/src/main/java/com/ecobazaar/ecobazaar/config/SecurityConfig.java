package com.ecobazaar.ecobazaar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;

import com.ecobazaar.ecobazaar.jwt.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)  // enables @PreAuthorize and @RolesAllowed

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF because we use JWT, not cookies
            .csrf(csrf -> csrf.disable())

         // Authorize requests by path and role
            .authorizeHttpRequests(auth -> auth

                // 🔓 Public endpoints (no login required)
                .requestMatchers("/api/auth/**").permitAll()        // register/login
                .requestMatchers("/api/verify/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/products/*/qrcode/download").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
            

                // 🧑‍🌾 Product APIs — only Farmers can create/edit
                .requestMatchers(HttpMethod.POST,"/api/products/upload")
                    .hasAnyRole("FARMER")
                .requestMatchers(HttpMethod.POST,"/api/products/*/qrcode")
                    .hasAnyRole("FARMER","ADMIN")

                // 🔗 Supply Chain Tracking APIs
                // Only Distributor/Retailer/Admin can update
                .requestMatchers("/api/track/update")
                    .hasAnyRole("DISTRIBUTOR","RETAILER","ADMIN")

                // All users (even without login) can view product journeys
               

                // 🧑‍💼 Admin routes (overview, management)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Anything else must be authenticated
                .anyRequest().authenticated()
            )

            // No sessions; JWT is stateless
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Add our JWT filter before Spring’s default login filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}