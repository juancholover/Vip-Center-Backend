package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para el control de presencia: quién está en el gimnasio ahora.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenciaGimnasioDTO {
    private Integer totalPresentes;
    private List<ClientePresenteDTO> presentes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientePresenteDTO {
        private Long asistenciaId;
        private Long clienteId;
        private String nombreCompleto;
        private String horaEntrada;           // "14:30"
        private String tiempoTranscurrido;    // "2h 15min"
        private String membresiaActual;
    }
}
