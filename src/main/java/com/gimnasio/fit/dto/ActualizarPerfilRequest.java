package com.gimnasio.fit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar el perfil del usuario autenticado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilRequest {
    
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;
    
    @Email(message = "El email debe ser válido")
    private String email;
    
    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    private String telefono;
}
