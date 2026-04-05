package com.gimnasio.fit.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;   // "Bearer"
    private Long expiresIn;     // segundos (access)
    private Long userId; // ✅ Cambiado de Integer a Long
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private Set<String> roles;
    private Set<String> permisos;
    
    /**
     * Indica si el usuario debe cambiar su contraseña.
     * Si es true, el frontend debe redirigir a pantalla de cambio de contraseña.
     */
    private Boolean debeCambiarPassword;
}