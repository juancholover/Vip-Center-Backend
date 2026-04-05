package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadRecienteDTO {
    private Long id;
    private String tipo; // "pago", "registro", "asistencia"
    private String mensaje;
    private String tiempo;
    private String icono;
}
