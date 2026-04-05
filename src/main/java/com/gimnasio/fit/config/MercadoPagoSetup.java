package com.gimnasio.fit.config;

import com.mercadopago.MercadoPagoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class MercadoPagoSetup {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        System.out.println("✅ MercadoPago inicializado.");
    }
}
