package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para permisos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermisoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String modulo;
}
