package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un rol en las respuestas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolDTO {
    private Long id;
    private String nombre;
    private String descripcion;
}
