package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para historial de accesos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialAccesoResponse {
    private Long id;
    private String accion;
    private LocalDateTime fecha;
    private String ip;
    private String dispositivo;
    private Boolean exitoso;
}
