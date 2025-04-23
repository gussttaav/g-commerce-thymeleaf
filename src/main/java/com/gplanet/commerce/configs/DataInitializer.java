package com.gplanet.commerce.configs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.repositories.UsuarioRepository;

import java.time.LocalDateTime;

/**
 * Configuration class responsible for initializing default data in the application.
 * Creates the default admin user if no users exist in the system.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    /**
     * Creates a CommandLineRunner bean that initializes the default admin user.
     * Only creates the admin if no users exist in the system and admin credentials are configured.
     * @return CommandLineRunner instance
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (adminProperties.getEmail() == null || adminProperties.getPassword() == null) {
                log.warn("Admin credentials not configured. Skipping admin user creation.");
                return;
            }

            // Only create admin if no users exist in the system
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setNombre("Admin");
                admin.setEmail(adminProperties.getEmail());
                admin.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
                admin.setRol(Usuario.Role.ADMIN);
                admin.setFechaCreacion(LocalDateTime.now());
                usuarioRepository.save(admin);
                log.info("Default admin user created successfully");
            }
        };
    }
}
