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
}
