package com.gimnasio.fit.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para actualizar un rol existente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarRolRequest {
    
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;
    
    @Size(max = 200, message = "La descripción no puede tener más de 200 caracteres")
    private String descripcion;
    
    private List<Long> permisosIds;
}
