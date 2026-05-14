package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el ranking de clientes con más asistencias (HU-26).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopClienteDTO {
    private Long id;            // ID del cliente
    private String nombre;      // Nombre completo del cliente
    private Integer asistencias; // Cantidad total de asistencias
}
