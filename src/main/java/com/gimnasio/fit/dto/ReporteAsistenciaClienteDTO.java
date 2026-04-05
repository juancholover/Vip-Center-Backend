package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAsistenciaClienteDTO {
    private Integer clienteId;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private Integer totalAsistencias;
    private LocalDate primeraAsistencia;
    private LocalDate ultimaAsistencia;
    private Double promedioAsistenciasMes;
    private String estadoMembresia;
    private LocalDate fechaVencimiento;
}
