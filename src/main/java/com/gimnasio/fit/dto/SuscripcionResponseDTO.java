package com.gimnasio.fit.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta completa tras activar/renovar suscripción.
 * Incluye información del QR para mostrar en el modal del frontend.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SuscripcionResponseDTO {
    
    // Datos de la suscripción
    private Long id;
    private Long clienteId;
    
    // Datos del cliente
    private ClienteInfoDTO cliente;
    
    // Datos de la membresía
    private MembresiaInfoDTO membresia;
    
    // Detalles del pago
    private BigDecimal montoFinal;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private String estado; // "ACTIVA", "VENCIDA"
    
    // QR de acceso (UUID desde tabla clientes)
    private String qrAcceso; // UUID único del cliente para control de acceso
    
    // ==========================================
    // DTOs anidados para información estructurada
    // ==========================================
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ClienteInfoDTO {
        private Long id;
        private String nombre;
        private String apellido;
        private String nombreCompleto;
        private String email;
        private String telefono;
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class MembresiaInfoDTO {
        private Long id;
        private String tipo;
        private String nombre;
        private Integer duracionDias;
        private BigDecimal precio;
        private String color;
    }
}
