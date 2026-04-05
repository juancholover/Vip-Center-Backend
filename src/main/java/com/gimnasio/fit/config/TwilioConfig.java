package com.gimnasio.fit.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TwilioConfig {

    @Value("${app.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.twilio.auth-token:}")
    private String authToken;

    @Value("${app.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @PostConstruct
    public void init() {
        if (whatsappEnabled && accountSid != null && !accountSid.isBlank() && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            log.info("✅ Twilio inicializado correctamente para WhatsApp");
        } else {
            log.warn("⚠️ Twilio no configurado - WhatsApp deshabilitado");
        }
    }
}
