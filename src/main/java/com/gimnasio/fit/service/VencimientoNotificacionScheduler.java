package com.gimnasio.fit.service;

import com.gimnasio.fit.entity.Cliente;
import com.gimnasio.fit.entity.ConfiguracionNotificacion;
import com.gimnasio.fit.entity.RegistroNotificacion;
import com.gimnasio.fit.repository.ClienteRepository;
import com.gimnasio.fit.repository.ConfiguracionNotificacionRepository;
import com.gimnasio.fit.repository.RegistroNotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Scheduler para envío automático de recordatorios de vencimiento de membresía (HU-31).
 * 
 * Se ejecuta diariamente a las 6:00 AM y busca clientes cuya membresía vence
 * en exactamente 30, 15, 7, 3 o 1 día(s). Envía un email de recordatorio y registra
 * el envío para evitar duplicados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VencimientoNotificacionScheduler {

    private final ClienteRepository clienteRepository;
    private final RegistroNotificacionRepository registroNotificacionRepository;
    private final ConfiguracionNotificacionRepository configRepository;
    private final JavaMailSender mailSender;

    @Value("${app.email.from:VIP Center Fit <no-reply@vipcentergym.com>}")
    private String emailFrom;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /** Días antes del vencimiento en los que se envía recordatorio */
    private static final int[] DIAS_RECORDATORIO = {30, 15, 7, 3, 1};

    /**
     * Tarea programada que se ejecuta cada día a las 6:00 AM.
     * Busca clientes próximos a vencer y envía recordatorios por email.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void enviarRecordatoriosVencimiento() {
        log.info("⏰ ========== INICIO: Recordatorios de vencimiento ==========");

        // Verificar si email está habilitado
        ConfiguracionNotificacion config = configRepository.findFirstByOrderByIdAsc().orElse(null);
        boolean emailActivo = (config != null && config.getEmailEnabled() != null)
                ? config.getEmailEnabled()
                : emailEnabled;

        if (!emailActivo) {
            log.info("📧 Email deshabilitado. Se omite envío de recordatorios.");
            return;
        }

        LocalDate hoy = LocalDate.now();
        int totalEnviados = 0;
        int totalErrores = 0;

        for (int diasAntes : DIAS_RECORDATORIO) {
            LocalDate fechaVencimiento = hoy.plusDays(diasAntes);
            log.info("🔍 Buscando clientes que vencen el {} (en {} días)...", fechaVencimiento, diasAntes);

            // Buscar clientes cuya membresía vence exactamente en esa fecha
            List<Cliente> clientes = clienteRepository.findByFechaVencimiento(fechaVencimiento);

            for (Cliente cliente : clientes) {
                // Verificar que el cliente tiene email
                if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
                    log.warn("⚠️ Cliente {} no tiene email. Omitiendo.", cliente.getId());
                    continue;
                }

                // Verificar que no se haya enviado ya esta notificación
                boolean yaEnviado = registroNotificacionRepository.existeNotificacionEnviada(
                        cliente.getId(), "EMAIL", diasAntes, fechaVencimiento);

                if (yaEnviado) {
                    log.debug("⏭️ Ya se envió recordatorio a cliente {} para {} días antes (vence {})",
                            cliente.getId(), diasAntes, fechaVencimiento);
                    continue;
                }

                // Enviar email
                boolean exitoso = enviarEmailRecordatorio(cliente, diasAntes, config);

                // Registrar el envío
                RegistroNotificacion registro = RegistroNotificacion.builder()
                        .cliente(cliente)
                        .tipo("EMAIL")
                        .diasAntes(diasAntes)
                        .fechaVencimientoReferencia(fechaVencimiento)
                        .fechaEnvio(LocalDateTime.now())
                        .exitoso(exitoso)
                        .mensajeError(exitoso ? null : "Error al enviar email")
                        .build();

                registroNotificacionRepository.save(registro);

                if (exitoso) {
                    totalEnviados++;
                } else {
                    totalErrores++;
                }
            }
        }

        log.info("✅ ========== FIN: Recordatorios de vencimiento ==========");
        log.info("📊 Resumen: {} enviados, {} errores", totalEnviados, totalErrores);
    }

    /**
     * Envía un email de recordatorio de vencimiento al cliente.
     *
     * @param cliente   Cliente al que se le envía el recordatorio
     * @param diasAntes Días que faltan para el vencimiento
     * @param config    Configuración de notificaciones (puede ser null)
     * @return true si se envió correctamente
     */
    private boolean enviarEmailRecordatorio(Cliente cliente, int diasAntes, ConfiguracionNotificacion config) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String from = (config != null && config.getEmailFrom() != null && !config.getEmailFrom().isBlank())
                    ? config.getEmailFrom() : emailFrom;

            helper.setFrom(from);
            helper.setTo(cliente.getEmail());
            helper.setSubject(generarAsuntoEmail(diasAntes));
            helper.setText(generarCuerpoEmailHtml(cliente, diasAntes), true); // true = HTML

            mailSender.send(mimeMessage);
            log.info("✅ Email de recordatorio enviado a {} ({}d antes)", cliente.getEmail(), diasAntes);
            return true;

        } catch (Exception e) {
            log.error("❌ Error al enviar email a {}: {}", cliente.getEmail(), e.getMessage());
            return false;
        }
    }

    /**
     * Genera el asunto del email según los días antes del vencimiento.
     */
    private String generarAsuntoEmail(int diasAntes) {
        if (diasAntes == 1) {
            return "⚠️ ¡Tu membresía vence MAÑANA! - VIP Center Fit";
        } else if (diasAntes <= 7) {
            return "⏰ Tu membresía vence en " + diasAntes + " días - VIP Center Fit";
        } else {
            return "📅 Recordatorio: Tu membresía vence en " + diasAntes + " días - VIP Center Fit";
        }
    }

    /**
     * Genera el cuerpo del email en HTML con diseño profesional.
     */
    private String generarCuerpoEmailHtml(Cliente cliente, int diasAntes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaVencimiento = cliente.getFechaVencimiento() != null
                ? cliente.getFechaVencimiento().format(formatter) : "N/A";

        String nombrePlan = cliente.getMembresiaActual() != null
                ? cliente.getMembresiaActual().getNombre() : "Sin plan";

        String colorUrgencia;
        String icono;
        String mensajeUrgencia;

        if (diasAntes == 1) {
            colorUrgencia = "#dc2626";
            icono = "⚠️";
            mensajeUrgencia = "¡Tu membresía vence <strong>MAÑANA</strong>! Renueva ahora para no perder acceso.";
        } else if (diasAntes <= 7) {
            colorUrgencia = "#f59e0b";
            icono = "⏰";
            mensajeUrgencia = "Tu membresía vence en <strong>" + diasAntes + " días</strong>. ¡Renueva pronto!";
        } else {
            colorUrgencia = "#3b82f6";
            icono = "📅";
            mensajeUrgencia = "Tu membresía vence en <strong>" + diasAntes + " días</strong>. Te recordamos para que planifiques tu renovación.";
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="margin:0; padding:0; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f3f4f6;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #7c3aed 0%%, #4f46e5 100%%); padding: 30px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 24px;">💪 VIP Center Fit</h1>
                        <p style="color: #e0e7ff; margin: 8px 0 0 0; font-size: 14px;">Tu gimnasio de confianza</p>
                    </div>

                    <!-- Alert Banner -->
                    <div style="background-color: %s; padding: 15px 30px; text-align: center;">
                        <p style="color: #ffffff; margin: 0; font-size: 16px;">%s %s</p>
                    </div>

                    <!-- Content -->
                    <div style="padding: 30px;">
                        <h2 style="color: #1f2937; margin-top: 0;">Hola %s,</h2>

                        <div style="background-color: #f9fafb; border-radius: 8px; padding: 20px; margin: 20px 0;">
                            <h3 style="color: #4b5563; margin-top: 0;">📋 Detalles de tu membresía:</h3>
                            <table style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td style="padding: 8px 0; color: #6b7280;">Plan:</td>
                                    <td style="padding: 8px 0; color: #1f2937; font-weight: bold; text-align: right;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 0; color: #6b7280;">Fecha de vencimiento:</td>
                                    <td style="padding: 8px 0; color: %s; font-weight: bold; text-align: right;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 0; color: #6b7280;">Días restantes:</td>
                                    <td style="padding: 8px 0; color: %s; font-weight: bold; text-align: right;">%d día(s)</td>
                                </tr>
                            </table>
                        </div>

                        <p style="color: #4b5563; line-height: 1.6;">
                            Renueva tu membresía a tiempo para seguir disfrutando de todos nuestros servicios
                            y equipos premium. ¡Te esperamos!
                        </p>

                        <div style="text-align: center; margin: 30px 0;">
                            <a href="#" style="display: inline-block; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: #ffffff; text-decoration: none; padding: 14px 36px; border-radius: 8px; font-weight: bold; font-size: 16px;">
                                Renovar Membresía
                            </a>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style="background-color: #1f2937; padding: 20px 30px; text-align: center;">
                        <p style="color: #9ca3af; margin: 0; font-size: 12px;">
                            VIP Center Fit — Este es un mensaje automático. Por favor no responda a este correo.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                colorUrgencia,
                icono,
                mensajeUrgencia,
                cliente.getNombreCompleto(),
                nombrePlan,
                colorUrgencia,
                fechaVencimiento,
                colorUrgencia,
                diasAntes
        );
    }
}
