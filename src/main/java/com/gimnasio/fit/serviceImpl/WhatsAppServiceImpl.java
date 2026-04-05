package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.service.WhatsAppService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WhatsAppServiceImpl implements WhatsAppService {

    @Value("${app.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @Value("${app.twilio.whatsapp-from:whatsapp:+14155238886}")
    private String twilioWhatsAppFrom;

    @Override
    public boolean enviarWhatsAppConImagen(String toNumber, String message, String imageUrl) {
        if (!whatsappEnabled) {
            log.warn("⚠️ WhatsApp deshabilitado - no se envió mensaje");
            return false;
        }

        try {
            // Formatear número: debe incluir whatsapp: y código de país
            String formattedNumber = formatearNumeroWhatsApp(toNumber);
            
            // Crear lista de URLs de medios
            List<URI> mediaUrl = new ArrayList<>();
            if (imageUrl != null && !imageUrl.isBlank()) {
                mediaUrl.add(URI.create(imageUrl));
            }

            // Enviar mensaje con imagen usando Twilio
            Message twilioMessage = Message.creator(
                    new PhoneNumber(formattedNumber),  // To
                    new PhoneNumber(twilioWhatsAppFrom), // From (Twilio WhatsApp Sandbox)
                    message  // Body
            )
            .setMediaUrl(mediaUrl)  // Imagen adjunta
            .create();

            log.info("✅ WhatsApp enviado exitosamente - SID: {}", twilioMessage.getSid());
            return true;

        } catch (Exception e) {
            log.error("❌ Error al enviar WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean enviarWhatsApp(String toNumber, String message) {
        return enviarWhatsAppConImagen(toNumber, message, null);
    }

    /**
     * Formatea el número de teléfono al formato requerido por Twilio WhatsApp
     * Input: 927073969 o +51927073969
     * Output: whatsapp:+51927073969
     */
    private String formatearNumeroWhatsApp(String numero) {
        // Limpiar el número
        String clean = numero.replaceAll("[^0-9+]", "");
        
        // Si no tiene +51, agregarlo
        if (!clean.startsWith("+51")) {
            if (clean.startsWith("51")) {
                clean = "+" + clean;
            } else if (clean.startsWith("9")) {
                clean = "+51" + clean;
            }
        }
        
        // Agregar prefijo whatsapp:
        if (!clean.startsWith("whatsapp:")) {
            clean = "whatsapp:" + clean;
        }
        
        return clean;
    }
}
