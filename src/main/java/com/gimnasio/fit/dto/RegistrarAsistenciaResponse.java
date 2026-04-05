package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de registro de asistencia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarAsistenciaResponse {
    private boolean success;
    private String mensaje;
    private Long asistenciaId;
    private LocalDateTime fechaHora;
    private ClienteAsistenciaDTO cliente;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteAsistenciaDTO {
        private Long id;
        private String nombre;
        private String apellido;
        private String estado;
        private MembresiaSimplifcadaDTO membresia;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MembresiaSimplifcadaDTO {
        private String nombre;
        private String color;
    }
}
