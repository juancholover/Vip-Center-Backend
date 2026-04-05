package com.gimnasio.fit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 🔐 Punto de entrada JWT global.
 * Maneja errores de autenticación y devuelve respuestas JSON consistentes.
 *
 * Incluye:
 *  - Token expirado o manipulado
 *  - Falta de credenciales
 *  - Token en blacklist
 *  - Errores inesperados
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 🧠 Detectar error capturado por JwtAuthenticationFilter
        Object jwtError = request.getAttribute("jwt_error");
        String message = "Credenciales inválidas o token no proporcionado";
        String errorCode = "unauthorized";

        if (jwtError != null) {
            String msg = jwtError.toString().toLowerCase();
            message = jwtError.toString();

            if (msg.contains("expirado")) {
                errorCode = "token_expired";
            } else if (msg.contains("invalidado")) {
                errorCode = "token_revoked";
            } else if (msg.contains("firma")) {
                errorCode = "invalid_signature";
            } else if (msg.contains("manipulado")) {
                errorCode = "tampered_token";
            } else if (msg.contains("malformado")) {
                errorCode = "malformed_token";
            }
        }

        // 🧾 Construcción de respuesta JSON
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", Instant.now().toString());
        errorDetails.put("status", 401);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("code", errorCode);
        errorDetails.put("message", message);
        errorDetails.put("path", request.getServletPath());

        // 🔥 Loguear para trazabilidad
        log.warn("🚫 Acceso no autorizado [{}]: {}", errorCode, message);

        // 📤 Enviar respuesta
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}
