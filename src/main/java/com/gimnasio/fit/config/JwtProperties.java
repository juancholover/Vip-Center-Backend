package com.gimnasio.fit.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JwtProperties {

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.access-expiration-seconds}")
    private Long accessExpirationSeconds;

    @Value("${app.security.jwt.refresh-expiration-seconds}")
    private Long refreshExpirationSeconds;

}