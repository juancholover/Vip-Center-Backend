package com.gimnasio.fit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para crear pagos con Yape.
 * El token es generado en el frontend usando el SDK JS de MercadoPago
 * tras capturar el OTP y número de teléfono del cliente.
 */
@Data
public class PagoYapeRequestDto {
    
    /**
     * Token Yape generado en frontend con mp.yape({otp, phoneNumber}).create()
     */
    @NotBlank(message = "token es requerido")
    private String token;
    
    /**
     * ID del cliente que realiza el pago
     */
    @NotNull(message = "clienteId es requerido")
    private Long clienteId;
    
    /**
     * Monto a pagar
     */
    @NotNull(message = "monto es requerido")
    private Double monto;
    
    /**
     * Nombre del plan (MENSUAL, TRIMESTRAL, etc.)
     */
    private String planNombre;
    
    /**
     * Días de duración del plan
     */
    private Integer planDias;
    
    /**
     * ID de la membresía seleccionada
     */
    private Long membresiaId;
    
    /**
     * Email del cliente
     */
    private String emailCliente;
    
    /**
     * Descripción del pago
     */
    private String descripcion;
}
