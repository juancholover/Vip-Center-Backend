package com.gimnasio.fit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para solicitud de cambio de contraseña obligatorio (primer login).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarPasswordRequest {

    /**
     * Nueva contraseña (mínimo 8 caracteres).
     */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String nuevaPassword;

    /**
     * Confirmación de la nueva contraseña (debe coincidir).
     */
    @NotBlank(message = "Debe confirmar la nueva contraseña")
    private String confirmarPassword;
}
