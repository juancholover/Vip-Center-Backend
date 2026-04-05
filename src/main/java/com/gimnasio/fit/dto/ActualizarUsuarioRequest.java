package com.gimnasio.fit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para actualizar un usuario/empleado existente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUsuarioRequest {
    
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;
    
    @Email(message = "El email debe ser válido")
    private String email;
    
    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    private String telefono;
    
    private List<Long> rolesIds;
    
    private Boolean activo;
}
