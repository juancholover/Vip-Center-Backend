package com.gimnasio.fit.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO para respuesta de reembolso procesado.
 */
@Data
@Builder
public class ReembolsoResponseDto {
    
    /**
     * ID del reembolso en MercadoPago
     */
    private String refundId;
    
    /**
     * ID del pago original
     */
    private String paymentId;
    
    /**
     * Monto reembolsado
     */
    private Double montoReembolsado;
    
    /**
     * Estado del reembolso (approved, pending, rejected)
     */
    private String status;
    
    /**
     * Mensaje descriptivo
     */
    private String message;
    
    /**
     * Fecha estimada de devolución del dinero
     */
    private String fechaEstimada;
}
