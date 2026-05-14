package com.gimnasio.fit.service;

import com.gimnasio.fit.entity.Cliente;
import com.gimnasio.fit.entity.Pago;
import com.gimnasio.fit.entity.ConfiguracionNotificacion;
import com.gimnasio.fit.repository.ConfiguracionNotificacionRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para enviar notificaciones por email y SMS.
 */
@Service
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Autowired
    private ConfiguracionNotificacionRepository configRepository;

    @Value("${app.email.from:VIP Center Fit <no-reply@vipcentergym.com>}")
    private String emailFrom;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${app.sms.twilio.from-number:}")
    private String twilioFromNumber;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía notificación de pago aprobado por email y SMS.
     * Se ejecuta de forma asíncrona para no bloquear el flujo principal.
     */
    @Async
    public void notificarPagoAprobado(Cliente cliente, Pago pago) {
        try {
            log.info("📧 Enviando notificación de pago aprobado a cliente: {}", cliente.getId());

            // Leer configuración de BD (prioridad) o usar properties como fallback
            ConfiguracionNotificacion config = configRepository.findFirstByOrderByIdAsc().orElse(null);
            boolean emailActivo = (config != null && config.getEmailEnabled() != null) ? config.getEmailEnabled() : emailEnabled;
            boolean smsActivo = (config != null && config.getSmsEnabled() != null) ? config.getSmsEnabled() : smsEnabled;

            // Enviar email
            if (emailActivo && cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
                enviarEmailPagoAprobado(cliente, pago, config);
            }

            // Enviar SMS
            if (smsActivo && cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) {
                enviarSmsPagoAprobado(cliente, pago, config);
            }

        } catch (Exception e) {
            log.error("❌ Error al enviar notificación: {}", e.getMessage(), e);
            // No lanzamos excepción para no afectar el flujo principal
        }
    }

    /**
     * Envía email de pago aprobado.
     */
    private void enviarEmailPagoAprobado(Cliente cliente, Pago pago, ConfiguracionNotificacion config) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String from = (config != null && config.getEmailFrom() != null && !config.getEmailFrom().isBlank()) ? config.getEmailFrom() : emailFrom;
            message.setFrom(from);
            message.setTo(cliente.getEmail());
            message.setSubject("✅ Pago Aprobado - VIP Center Fit");
            message.setText(construirMensajeEmailPago(cliente, pago));

            mailSender.send(message);
            log.info("✅ Email enviado a: {}", cliente.getEmail());

        } catch (Exception e) {
            log.error("❌ Error al enviar email: {}", e.getMessage());
        }
    }

    /**
     * Envía SMS de pago aprobado usando Twilio.
     */
    private void enviarSmsPagoAprobado(Cliente cliente, Pago pago, ConfiguracionNotificacion config) {
        try {
            // Usar config de BD si existe, sino usar properties
            String accountSid = (config != null && config.getTwilioAccountSid() != null && !config.getTwilioAccountSid().isBlank()) ? config.getTwilioAccountSid() : twilioAccountSid;
            String authToken = (config != null && config.getTwilioAuthToken() != null && !config.getTwilioAuthToken().isBlank()) ? config.getTwilioAuthToken() : twilioAuthToken;
            String fromNumber = (config != null && config.getTwilioFromNumber() != null && !config.getTwilioFromNumber().isBlank()) ? config.getTwilioFromNumber() : twilioFromNumber;

            if (accountSid == null || accountSid.isBlank() ||
                authToken == null || authToken.isBlank()) {
                log.warn("⚠️ Credenciales de Twilio no configuradas. SMS no enviado.");
                return;
            }

            // Inicializar Twilio
            Twilio.init(accountSid, authToken);

            // Formatear número de teléfono (agregar código de país si falta)
            String phoneNumber = formatearTelefono(cliente.getTelefono());

            // Crear mensaje
            Message sms = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromNumber),
                    construirMensajeSmsPago(cliente, pago)
            ).create();

            log.info("✅ SMS enviado a: {} (SID: {})", phoneNumber, sms.getSid());

        } catch (Exception e) {
            log.error("❌ Error al enviar SMS: {}", e.getMessage());
        }
    }

    /**
     * Construye el mensaje de email para pago aprobado.
     */
    private String construirMensajeEmailPago(Cliente cliente, Pago pago) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaVencimiento = cliente.getFechaVencimiento() != null 
                ? cliente.getFechaVencimiento().format(formatter) 
                : "N/A";

        return String.format("""
            Hola %s,
            
            ¡Tu pago ha sido aprobado exitosamente!
            
            📋 Detalles del pago:
            - Plan: %s
            - Monto: S/ %.2f
            - Duración: %d días
            - Estado: %s
            
            📅 Tu membresía es válida hasta: %s
            
            🎫 Tu código QR de acceso: %s
            (Usa este código para ingresar al gimnasio)
            
            ¡Gracias por confiar en VIP Center Fit!
            
            ---
            VIP Center Fit - Tu gimnasio de confianza
            """,
                cliente.getNombreCompleto(),
                pago.getPlanNombre(),
                pago.getMontoFinal(),
                pago.getPlanDias(),
                pago.getEstado(),
                fechaVencimiento,
                cliente.getQrAcceso() != null ? cliente.getQrAcceso() : "Generando..."
        );
    }

    /**
     * Construye el mensaje SMS (más corto que el email).
     */
    private String construirMensajeSmsPago(Cliente cliente, Pago pago) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaVencimiento = cliente.getFechaVencimiento() != null 
                ? cliente.getFechaVencimiento().format(formatter) 
                : "N/A";

        return String.format(
            "✅ VIP Center Fit: Pago aprobado! Plan %s por S/ %.2f. Valido hasta %s. QR: %s",
            pago.getPlanNombre(),
            pago.getMontoFinal(),
            fechaVencimiento,
            cliente.getQrAcceso() != null ? cliente.getQrAcceso().substring(0, 8) + "..." : "Generando"
        );
    }

    /**
     * Formatea número de teléfono agregando código de país si falta.
     * Ejemplo: 987654321 -> +51987654321
     */
    private String formatearTelefono(String telefono) {
        // Remover espacios y caracteres no numéricos
        String clean = telefono.replaceAll("[^0-9+]", "");
        
        // Si no tiene código de país, agregar +51 (Perú)
        if (!clean.startsWith("+")) {
            clean = "+51" + clean;
        }
        
        return clean;
    }

    /**
     * Envía notificación de reembolso procesado.
     */
    @Async
    public void notificarReembolso(Cliente cliente, Pago pago, BigDecimal montoReembolsado) {
        try {
            log.info("📧 Enviando notificación de reembolso a cliente: {}", cliente.getId());

            // Leer configuración (BD prioritaria)
            ConfiguracionNotificacion config = configRepository.findFirstByOrderByIdAsc().orElse(null);
            boolean emailActivo = (config != null && config.getEmailEnabled() != null) ? config.getEmailEnabled() : emailEnabled;
            boolean smsActivo = (config != null && config.getSmsEnabled() != null) ? config.getSmsEnabled() : smsEnabled;

            if (emailActivo && cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
                enviarEmailReembolso(cliente, pago, montoReembolsado, config);
            }

            if (smsActivo && cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) {
                enviarSmsReembolso(cliente, pago, montoReembolsado, config);
            }

        } catch (Exception e) {
            log.error("❌ Error al enviar notificación de reembolso: {}", e.getMessage(), e);
        }
    }

    /**
     * Envía email de reembolso procesado.
     */
    private void enviarEmailReembolso(Cliente cliente, Pago pago, BigDecimal montoReembolsado, ConfiguracionNotificacion config) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String from = (config != null && config.getEmailFrom() != null && !config.getEmailFrom().isBlank()) ? config.getEmailFrom() : emailFrom;
            message.setFrom(from);
            message.setTo(cliente.getEmail());
            message.setSubject("💰 Reembolso Procesado - VIP Center Fit");
            message.setText(String.format("""
                Hola %s,
                
                Tu reembolso ha sido procesado exitosamente.
                
                📋 Detalles:
                - Pago original: %s
                - Monto reembolsado: S/ %.2f
                - ID de pago: %s
                
                El dinero será devuelto a tu método de pago original en 5-10 días hábiles.
                
                Si tienes alguna pregunta, no dudes en contactarnos.
                
                ---
                VIP Center Fit
                """,
                    cliente.getNombreCompleto(),
                    pago.getPlanNombre(),
                    montoReembolsado,
                    pago.getMpPaymentId()
            ));

            mailSender.send(message);
            log.info("✅ Email de reembolso enviado a: {}", cliente.getEmail());

        } catch (Exception e) {
            log.error("❌ Error al enviar email de reembolso: {}", e.getMessage());
        }
    }

    /**
     * Envía SMS de reembolso procesado.
     */
    private void enviarSmsReembolso(Cliente cliente, Pago pago, BigDecimal montoReembolsado, ConfiguracionNotificacion config) {
        try {
            String accountSid = (config != null && config.getTwilioAccountSid() != null && !config.getTwilioAccountSid().isBlank()) ? config.getTwilioAccountSid() : twilioAccountSid;
            String authToken = (config != null && config.getTwilioAuthToken() != null && !config.getTwilioAuthToken().isBlank()) ? config.getTwilioAuthToken() : twilioAuthToken;
            String fromNumber = (config != null && config.getTwilioFromNumber() != null && !config.getTwilioFromNumber().isBlank()) ? config.getTwilioFromNumber() : twilioFromNumber;

            if (accountSid == null || accountSid.isBlank()) return;

            Twilio.init(accountSid, authToken);
            String phoneNumber = formatearTelefono(cliente.getTelefono());

        Message sms = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromNumber),
                    String.format("💰 VIP Center Fit: Reembolso procesado de S/ %.2f. Llegara en 5-10 dias.", montoReembolsado)
            ).create();

        log.info("✅ SMS de reembolso enviado a: {} (SID: {})", phoneNumber, sms.getSid());

        } catch (Exception e) {
            log.error("❌ Error al enviar SMS de reembolso: {}", e.getMessage());
        }
    }

    /**
     * Envía notificación de pago fallido/rechazado al cliente (HU-33).
     * Se ejecuta de forma asíncrona para no bloquear el flujo principal.
     * 
     * @param cliente Cliente afectado
     * @param pago    Pago rechazado/pendiente
     * @param motivo  Detalle del motivo del fallo
     * @return true si se envió correctamente
     */
    @Async
    public boolean notificarPagoFallido(Cliente cliente, Pago pago, String motivo) {
        try {
            log.info("⚠️ Enviando notificación de pago fallido a cliente: {}", cliente.getId());

            // Leer configuración
            ConfiguracionNotificacion config = configRepository.findFirstByOrderByIdAsc().orElse(null);
            boolean emailActivo = (config != null && config.getEmailEnabled() != null) ? config.getEmailEnabled() : emailEnabled;

            if (!emailActivo) {
                log.info("📧 Email deshabilitado. No se envía alerta de pago fallido.");
                return false;
            }

            if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
                log.warn("⚠️ Cliente {} no tiene email. No se envía alerta.", cliente.getId());
                return false;
            }

            // Enviar email HTML
            String from = (config != null && config.getEmailFrom() != null && !config.getEmailFrom().isBlank())
                    ? config.getEmailFrom() : emailFrom;

            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = 
                new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(cliente.getEmail());
            helper.setSubject("⚠️ Problema con tu pago - VIP Center Fit");
            helper.setText(construirEmailPagoFallido(cliente, pago, motivo), true);

            mailSender.send(mimeMessage);
            log.info("✅ Email de alerta de pago fallido enviado a: {}", cliente.getEmail());
            return true;

        } catch (Exception e) {
            log.error("❌ Error al enviar notificación de pago fallido: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Construye el cuerpo HTML del email de alerta de pago fallido (HU-33).
     */
    private String construirEmailPagoFallido(Cliente cliente, Pago pago, String motivo) {
        String montoStr = pago.getMontoFinal() != null ? String.format("S/ %.2f", pago.getMontoFinal()) : "N/A";
        String planStr = pago.getPlanNombre() != null ? pago.getPlanNombre() : "Membresía";
        String motivoStr = motivo != null ? motivo : "Motivo no especificado";
        String estadoStr = pago.getEstado() != null ? pago.getEstado() : "rechazado";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0; padding:0; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f3f4f6;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #dc2626 0%%, #991b1b 100%%); padding: 30px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 24px;">⚠️ Problema con tu pago</h1>
                        <p style="color: #fecaca; margin: 8px 0 0 0; font-size: 14px;">VIP Center Fit</p>
                    </div>

                    <!-- Content -->
                    <div style="padding: 30px;">
                        <h2 style="color: #1f2937; margin-top: 0;">Hola %s,</h2>

                        <p style="color: #4b5563; line-height: 1.6;">
                            Lamentamos informarte que tu pago no pudo ser procesado correctamente.
                        </p>

                        <div style="background-color: #fef2f2; border-left: 4px solid #dc2626; border-radius: 4px; padding: 16px; margin: 20px 0;">
                            <p style="color: #991b1b; margin: 0; font-weight: bold;">Estado: %s</p>
                            <p style="color: #7f1d1d; margin: 8px 0 0 0;">Motivo: %s</p>
                        </div>

                        <div style="background-color: #f9fafb; border-radius: 8px; padding: 20px; margin: 20px 0;">
                            <h3 style="color: #4b5563; margin-top: 0;">📋 Detalles del pago:</h3>
                            <table style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td style="padding: 8px 0; color: #6b7280;">Plan:</td>
                                    <td style="padding: 8px 0; color: #1f2937; font-weight: bold; text-align: right;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 0; color: #6b7280;">Monto:</td>
                                    <td style="padding: 8px 0; color: #1f2937; font-weight: bold; text-align: right;">%s</td>
                                </tr>
                            </table>
                        </div>

                        <p style="color: #4b5563; line-height: 1.6;">
                            <strong>¿Qué puedes hacer?</strong>
                        </p>
                        <ul style="color: #4b5563; line-height: 1.8;">
                            <li>Verifica que tu tarjeta tenga fondos suficientes</li>
                            <li>Actualiza tu método de pago e intenta nuevamente</li>
                            <li>Contacta a tu banco si el problema persiste</li>
                            <li>Visítanos en recepción para pagar en efectivo o por Yape</li>
                        </ul>

                        <div style="text-align: center; margin: 30px 0;">
                            <a href="#" style="display: inline-block; background: linear-gradient(135deg, #7c3aed, #4f46e5); color: #ffffff; text-decoration: none; padding: 14px 36px; border-radius: 8px; font-weight: bold; font-size: 16px;">
                                Reintentar Pago
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
                cliente.getNombreCompleto(),
                estadoStr.toUpperCase(),
                motivoStr,
                planStr,
                montoStr
        );
    }
}
