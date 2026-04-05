package com.gimnasio.fit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar la contraseña desde el perfil
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarPasswordPerfilRequest {
    
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;
    
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String passwordNueva;
    
    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmarPassword;
}
