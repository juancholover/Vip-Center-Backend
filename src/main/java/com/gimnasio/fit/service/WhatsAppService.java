package com.gimnasio.fit.service;

public interface WhatsAppService {
    
    /**
     * Envía un mensaje de WhatsApp con imagen adjunta (QR)
     * @param toNumber Número del destinatario en formato +51XXXXXXXXX
     * @param message Mensaje de texto
     * @param imageUrl URL pública de la imagen (QR)
     * @return true si se envió correctamente
     */
    boolean enviarWhatsAppConImagen(String toNumber, String message, String imageUrl);
    
    /**
     * Envía un mensaje de WhatsApp simple sin imagen
     * @param toNumber Número del destinatario en formato +51XXXXXXXXX
     * @param message Mensaje de texto
     * @return true si se envió correctamente
     */
    boolean enviarWhatsApp(String toNumber, String message);
}
