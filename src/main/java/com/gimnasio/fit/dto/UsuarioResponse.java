package com.gimnasio.fit.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta para información de usuario/empleado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private Boolean activo;
    private Boolean debeCambiarPassword;
    private Instant fechaBloqueo;
    private List<RolDTO> roles; // Lista de objetos Rol completos
    private Instant fechaCreacion;
    private Instant fechaModificacion;
}