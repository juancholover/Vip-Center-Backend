package com.gimnasio.fit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para crear un nuevo rol
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearRolRequest {
    
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;
    
    @Size(max = 200, message = "La descripción no puede tener más de 200 caracteres")
    private String descripcion;
    
    @NotNull(message = "Debe asignar al menos un permiso")
    @Size(min = 1, message = "Debe asignar al menos un permiso")
    private List<Long> permisosIds;
}
