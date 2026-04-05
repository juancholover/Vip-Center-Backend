package com.gimnasio.fit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para crear un nuevo usuario/empleado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearUsuarioRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
    
    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    private String telefono;
    
    @NotNull(message = "Debe asignar al menos un rol")
    @Size(min = 1, message = "Debe asignar al menos un rol")
    private List<Long> rolesIds;
}