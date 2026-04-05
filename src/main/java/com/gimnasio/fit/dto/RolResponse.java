package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para roles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<PermisoResponse> permisos;
}
