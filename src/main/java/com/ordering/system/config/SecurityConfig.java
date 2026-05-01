package com.ordering.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on individual controller methods
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth

                // ── 1. Public pages — no login required ────────────────────
                .requestMatchers(
                    "/login", "/register", "/access-denied",
                    "/css/**", "/js/**", "/images/**", "/webjars/**"
                ).permitAll()

                // ── 2. All authenticated roles can access /orders ──────────
                .requestMatchers("/orders", "/orders/**")
                    .hasAnyRole("USER", "ADMIN", "MANAGER")

                // ── 3. Admin + Manager only ────────────────────────────────
                .requestMatchers("/items/**")    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/labor/**")    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/reports/**")  .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/dashboard/**").hasAnyRole("USER", "ADMIN", "MANAGER")

                // ── 4. Admin ONLY — user management ────────────────────────
                .requestMatchers("/users/**").hasRole("ADMIN")

                // ── 5. Anything else requires login ────────────────────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)   // admin/manager land on dashboard
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                // When a USER tries /items directly → custom 403 page
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}