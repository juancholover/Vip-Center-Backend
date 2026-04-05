package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteClienteProximoVencerDTO {
    private Integer clienteId;
    private String nombreCompleto;
    private String avatar;
    private String plan;
    private Integer diasRestantes;
    private String estado;
}
