package com.gimnasio.fit.dto;

import lombok.*;

/**
 * DTO de respuesta para logout exitoso.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutResponse {

    private String mensaje;
    
    /**
     * Timestamp de la invalidación.
     */
    private Long timestamp;

    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .mensaje("Sesión cerrada exitosamente")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
