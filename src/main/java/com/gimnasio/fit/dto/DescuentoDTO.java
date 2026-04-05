package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para operaciones CRUD de descuentos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoDTO {
    private Long id;
    private String nombre;
    private BigDecimal porcentaje;
    private Integer orden;
    private Boolean estado;
}
