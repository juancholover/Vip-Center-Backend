package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteClienteAusenteDTO {
    private Integer clienteId;
    private String nombreCompleto;
    private String avatar;
    private Integer diasAusente;
    private LocalDate ultimaVisita;
    private String estadoMembresia;
}
