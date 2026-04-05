package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteHoraPicoDTO {
    private String hora;         // "06:00", "18:00"
    private Integer intensidad;  // cantidad de asistencias
}
