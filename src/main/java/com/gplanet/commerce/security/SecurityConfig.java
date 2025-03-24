package com.gplanet.commerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/**
 * Security configuration class that sets up Spring Security for the application.
 * This class defines security rules, authentication, and authorization settings.
 *
 * @author Gustavo
 * @version 1.0
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetallesService customUserDetailsService;

    /**
     * Configures the security filter chain with specific security rules and permissions.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if there's an error during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/", "/usuarios/registro", "/usuarios/login").permitAll()
                .requestMatchers("/usuarios/admin/**").hasRole("ADMIN")
                .requestMatchers("/usuarios/perfil", "/usuarios/password").authenticated()
                .requestMatchers("/productos/listar").authenticated()
                .requestMatchers("/productos/**").hasRole("ADMIN")
                .requestMatchers("/compras/nueva").hasRole("USER")
                .requestMatchers("/compras/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/usuarios/login")
                .defaultSuccessUrl("/")
                .failureUrl("/usuarios/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/acceso-denegado")
            )
            .httpBasic(httpBasic -> httpBasic.realmName("MyAppRealm"));
        
        return http.build();
    }

    /**
     * Creates a password encoder bean for secure password hashing.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the authentication manager with custom user details service and password encoder.
     *
     * @param http the HttpSecurity to configure
     * @param passwordEncoder the password encoder to use
     * @return configured AuthenticationManager
     * @throws Exception if there's an error during configuration
     */
    @Bean
    AuthenticationManager authManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder);
        
        return authenticationManagerBuilder.build();
    }
}
