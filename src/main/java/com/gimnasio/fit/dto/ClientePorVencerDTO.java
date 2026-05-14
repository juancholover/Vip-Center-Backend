package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la bandeja de clientes por vencer con prioridad de contacto (HU-32).
 * Ordenado por días restantes ascendente (los de 1 día aparecen primero).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientePorVencerDTO {
    private Long clienteId;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String plan;                 // Nombre de la membresía
    private LocalDate fechaVencimiento;
    private Integer diasRestantes;       // Días para que venza (1, 2, 3...)
    private String estadoSeguimiento;    // "PENDIENTE", "LLAMADO", "PROMESA"
    private String avatar;               // Iniciales del nombre (ej: "JP")
}
