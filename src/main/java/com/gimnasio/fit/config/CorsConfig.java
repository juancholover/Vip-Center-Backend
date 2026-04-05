package com.gimnasio.fit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración CORS para permitir peticiones del frontend React (Vite)
 * 
 * Permite:
 * - Peticiones desde http://localhost:5173 (Vite dev server)
 * - Todos los métodos HTTP necesarios (GET, POST, PUT, DELETE, OPTIONS)
 * - Headers de autorización (Bearer JWT)
 * - Credenciales (cookies si se usan en el futuro)
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (frontend en desarrollo)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Vite dev server
            "http://localhost:3000"   // Por si usas otro puerto en el futuro
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "OPTIONS",
            "PATCH"
        ));
        
        // Headers permitidos (incluyendo Authorization para JWT)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));
        
        // Headers que el navegador puede exponer al frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Permitir credenciales (cookies, headers de autorización)
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests (1 hora)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
