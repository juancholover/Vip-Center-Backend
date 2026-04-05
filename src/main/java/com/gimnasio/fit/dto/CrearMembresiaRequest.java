package com.gimnasio.fit.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear o actualizar una membresía.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearMembresiaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La duración en días es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 día")
    @Max(value = 3650, message = "La duración no puede ser mayor a 10 años")
    private Integer duracionDias;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private Boolean estado = true;

    @Size(max = 20, message = "El color no puede tener más de 20 caracteres")
    private String color;

    @Min(value = 0, message = "El orden no puede ser negativo")
    private Integer orden = 0;
}
