package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAsistenciaRecienteDTO {
    private Integer asistenciaId;
    private String nombreCompleto;
    private String avatar;
    private String hora;
    private String membresia;
    private String estado;
}
