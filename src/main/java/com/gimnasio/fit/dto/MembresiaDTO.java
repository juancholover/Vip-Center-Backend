package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para mostrar información de una membresía.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembresiaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer duracionDias;
    private BigDecimal precio;
    private Boolean estado;
    private String color;
    private Integer orden;
}
