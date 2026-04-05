package com.gimnasio.fit.dto;

import com.gimnasio.fit.entity.TipoRegistro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para información de asistencia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaDTO {
    private Long id;
    private LocalDateTime fechaHora;
    private TipoRegistro tipoRegistro;
    private String dispositivo;
    private String ipAddress;
    private Double latitud;
    private Double longitud;
    private String notas;
    
    // Información del cliente
    private ClienteBasicoDTO cliente;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteBasicoDTO {
        private Long id;
        private String nombre;
        private String apellido;
        private String email;
        private String telefono;
        private String estado;  // "activo", "vencido", "sin_membresia", "qr_deshabilitado"
        private java.time.LocalDate fechaVencimiento;
        private MembresiaSimplifcadaDTO membresia;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MembresiaSimplifcadaDTO {
        private String nombre;
        private String color;
    }
}
