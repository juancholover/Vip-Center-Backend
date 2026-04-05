package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para almacenar la configuración de notificaciones (Email y SMS).
 * Solo debe existir 1 registro en la tabla.
 */
@Entity
@Table(name = "configuracion_notificacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== EMAIL ==========
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "email_from", nullable = false, length = 255)
    private String emailFrom = "VIP Center Fit <no-reply@vipcentergym.com>";

    // ========== SMS (TWILIO) ==========
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    @Column(name = "twilio_account_sid", length = 100)
    private String twilioAccountSid;

    @Column(name = "twilio_auth_token", length = 100)
    private String twilioAuthToken;

    @Column(name = "twilio_from_number", length = 20)
    private String twilioFromNumber;

    // ========== AUDITORIA ==========
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "modificado_por", length = 100)
    private String modificadoPor;

    @PreUpdate
    @PrePersist
    public void actualizarFecha() {
        this.ultimaModificacion = LocalDateTime.now();
    }
}
