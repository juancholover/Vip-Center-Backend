package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.ConfiguracionNotificacionDTO;
import com.gimnasio.fit.entity.ConfiguracionNotificacion;
import com.gimnasio.fit.repository.ConfiguracionNotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar la configuración de notificaciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionNotificacionService {

    private final ConfiguracionNotificacionRepository repository;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabledDefault;

    @Value("${app.email.from:VIP Center Fit <no-reply@vipcentergym.com>}")
    private String emailFromDefault;

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabledDefault;

    @Value("${app.sms.twilio.account-sid:}")
    private String twilioAccountSidDefault;

    @Value("${app.sms.twilio.auth-token:}")
    private String twilioAuthTokenDefault;

    @Value("${app.sms.twilio.from-number:}")
    private String twilioFromNumberDefault;

    /**
     * Obtiene la configuración actual.
     * Si no existe en BD, devuelve la configuración de application.properties
     */
    @Transactional(readOnly = true)
    public ConfiguracionNotificacionDTO obtenerConfiguracion() {
        return repository.findFirstByOrderByIdAsc()
                .map(this::convertirADTO)
                .orElseGet(this::obtenerConfiguracionPorDefecto);
    }

    /**
     * Guarda/actualiza la configuración.
     */
    @Transactional
    public ConfiguracionNotificacionDTO guardarConfiguracion(ConfiguracionNotificacionDTO dto, String usuarioModificador) {
        ConfiguracionNotificacion config = repository.findFirstByOrderByIdAsc()
                .orElse(new ConfiguracionNotificacion());

        // Actualizar campos
        config.setEmailEnabled(dto.getEmailEnabled());
        config.setEmailFrom(dto.getEmailFrom());
        config.setSmsEnabled(dto.getSmsEnabled());
        config.setTwilioAccountSid(dto.getTwilioAccountSid());
        config.setTwilioAuthToken(dto.getTwilioAuthToken());
        config.setTwilioFromNumber(dto.getTwilioFromNumber());
        config.setModificadoPor(usuarioModificador);

        ConfiguracionNotificacion guardada = repository.save(config);
        log.info("✅ Configuración de notificaciones actualizada por: {}", usuarioModificador);

        return convertirADTO(guardada);
    }

    /**
     * Convierte entidad a DTO.
     */
    private ConfiguracionNotificacionDTO convertirADTO(ConfiguracionNotificacion entity) {
        return new ConfiguracionNotificacionDTO(
                entity.getEmailEnabled(),
                entity.getEmailFrom(),
                entity.getSmsEnabled(),
                entity.getTwilioAccountSid(),
                entity.getTwilioAuthToken(),
                entity.getTwilioFromNumber()
        );
    }

    /**
     * Devuelve configuración por defecto desde properties.
     */
    private ConfiguracionNotificacionDTO obtenerConfiguracionPorDefecto() {
        log.info("⚠️ No hay configuración en BD, usando valores de application.properties");
        return new ConfiguracionNotificacionDTO(
                emailEnabledDefault,
                emailFromDefault,
                smsEnabledDefault,
                twilioAccountSidDefault,
                twilioAuthTokenDefault,
                twilioFromNumberDefault
        );
    }
}
