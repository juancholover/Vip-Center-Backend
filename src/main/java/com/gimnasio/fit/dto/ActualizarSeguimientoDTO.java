package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar el estado de seguimiento de un cliente (HU-32).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarSeguimientoDTO {
    private String estadoSeguimiento; // "PENDIENTE", "LLAMADO", "PROMESA"
}
