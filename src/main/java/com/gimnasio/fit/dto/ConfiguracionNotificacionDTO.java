package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir configuración de notificaciones al frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionNotificacionDTO {

    private Boolean emailEnabled;
    private String emailFrom;

    private Boolean smsEnabled;
    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioFromNumber;
}
