package com.gimnasio.fit.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con información completa del cliente.
 * 
 * Incluye:
 * - Datos personales
 * - Estado de membresía (calculado)
 * - Información de QR
 * - Auditoría
 */
@Getter
@Setter
@Builder
public class ClienteResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String dni;
    
    // Estado de membresía
    private String estado; // "activo", "vencido", "qr_deshabilitado"
    private LocalDate fechaVencimiento;
    private Boolean qrActivo;
    private String qrAcceso; // Solo incluir si tiene QR generado
    
    // Información de membresía actual
    private MembresiaInfoDTO membresiaActual;
    
    // Auditoría
    private String registradoPor; // Nombre del usuario que lo registró
    private Instant fechaRegistro;

    // Nueva: última asistencia del cliente (fecha y hora)
    private LocalDateTime ultimaAsistencia;

    /**
     * DTO simplificado con info básica de la membresía.
     */
    @Getter
    @Setter
    @Builder
    public static class MembresiaInfoDTO {
        private Long id;
        private String nombre;
        private Integer duracionDias;
        private String color;
    }
}
