package com.gimnasio.fit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para actualización de contraseña por usuario autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPasswordRequest {

    /**
     * Contraseña actual (para verificación).
     */
    @NotBlank(message = "Debe proporcionar la contraseña actual")
    private String passwordActual;

    /**
     * Nueva contraseña.
     */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String nuevaPassword;

    /**
     * Confirmación de nueva contraseña.
     */
    @NotBlank(message = "Debe confirmar la nueva contraseña")
    private String confirmarPassword;
}
