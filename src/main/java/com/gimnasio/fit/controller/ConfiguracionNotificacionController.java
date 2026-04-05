package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.ConfiguracionNotificacionDTO;
import com.gimnasio.fit.service.ConfiguracionNotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para gestionar la configuración de notificaciones.
 * Solo accesible por ADMIN.
 */
@RestController
@RequestMapping("/api/configuracion/notificaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConfiguracionNotificacionController {

    private final ConfiguracionNotificacionService service;

    /**
     * GET /api/configuracion/notificaciones
     * Obtiene la configuración actual de notificaciones.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionNotificacionDTO> obtenerConfiguracion() {
        log.info("📥 GET /api/configuracion/notificaciones");
        ConfiguracionNotificacionDTO config = service.obtenerConfiguracion();
        return ResponseEntity.ok(config);
    }

    /**
     * POST /api/configuracion/notificaciones
     * Guarda/actualiza la configuración de notificaciones.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionNotificacionDTO> guardarConfiguracion(
            @RequestBody ConfiguracionNotificacionDTO dto,
            Authentication authentication
    ) {
        String usuario = authentication.getName();
        log.info("📤 POST /api/configuracion/notificaciones por usuario: {}", usuario);

        // Validaciones
        if (Boolean.TRUE.equals(dto.getEmailEnabled()) && (dto.getEmailFrom() == null || dto.getEmailFrom().isBlank())) {
            return ResponseEntity.badRequest().build();
        }

        if (Boolean.TRUE.equals(dto.getSmsEnabled())) {
            if (dto.getTwilioAccountSid() == null || dto.getTwilioAccountSid().isBlank() ||
                dto.getTwilioAuthToken() == null || dto.getTwilioAuthToken().isBlank() ||
                dto.getTwilioFromNumber() == null || dto.getTwilioFromNumber().isBlank()) {
                return ResponseEntity.badRequest().build();
            }
        }

        ConfiguracionNotificacionDTO guardada = service.guardarConfiguracion(dto, usuario);
        return ResponseEntity.ok(guardada);
    }
}
