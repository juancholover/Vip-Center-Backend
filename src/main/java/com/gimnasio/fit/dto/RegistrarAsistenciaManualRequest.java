package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registro manual de asistencia por parte del staff.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarAsistenciaManualRequest {
    private Long clienteId;
    private String notas; // Opcional: motivo del registro manual
}
