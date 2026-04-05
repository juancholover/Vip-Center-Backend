package com.gimnasio.fit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para solicitar un reembolso de pago.
 */
@Data
public class ReembolsoRequestDto {
    
    /**
     * Motivo del reembolso (obligatorio)
     */
    @NotBlank(message = "El motivo del reembolso es requerido")
    private String motivo;
    
    /**
     * Monto a reembolsar (opcional, si no se especifica se reembolsa el monto total)
     */
    private Double monto;
}
