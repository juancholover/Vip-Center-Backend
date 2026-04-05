package com.gimnasio.fit.utils;

import com.gimnasio.fit.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Utilidad central para la generación, validación y análisis de tokens JWT.
 *
 *  - Genera access tokens (de corta duración)
 *  - Genera refresh tokens (de larga duración)
 *  - Permite verificar si un token está expirado o manipulado
 *  - Provee métodos para extraer claims del token
 *
 */
@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // =====================================================
    // 🧾 GENERACIÓN DE TOKENS
    // =====================================================

    /**
     * Genera un access token (válido por ~1 hora normalmente).
     * Contiene los roles y permisos del usuario en el claim.
     */
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getAccessExpirationSeconds());
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Genera un refresh token (válido por días o semanas).
     * Se usa exclusivamente para renovar el access token.
     */
    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getRefreshExpirationSeconds());
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }


    public String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public String extractUsernameFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) return null;
        try {
            return getSubject(token);
        } catch (Exception e) {
            return null;
        }
    }

    // =====================================================
    // 🕵️‍♂️ VALIDACIÓN Y ANÁLISIS
    // =====================================================

    /**
     * Parsea y valida un JWT.
     * Lanza excepciones específicas si el token está manipulado o expirado.
     */
    public Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Verifica si un token es de tipo refresh.
     */
    public boolean isRefreshToken(Jws<Claims> jws) {
        return "refresh".equals(jws.getPayload().get("type"));
    }

    /**
     * Retorna true si el token ha expirado.
     *
     * @param token JWT sin prefijo "Bearer "
     */
    public boolean isTokenExpired(String token) {
        try {
            var jws = parseToken(token);
            Date expiration = jws.getPayload().getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true; // token inválido o manipulado también se considera "no válido"
        }
    }

    // =====================================================
    // 📤 EXTRACCIÓN DE DATOS
    // =====================================================

    /**
     * Obtiene el "subject" (normalmente el email o username).
     */
    public String getSubject(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    /**
     * Obtiene los claims (roles, permisos, etc.) de un token.
     */
    public Claims getClaims(String token) {
        return parseToken(token).getPayload();
    }

    // =====================================================
    // ⚙️ UTILITARIOS ADICIONALES
    // =====================================================

    /**
     * Retorna la fecha de expiración de un token sin lanzar excepción.
     */
    public Date getExpiration(String token) {
        try {
            return parseToken(token).getPayload().getExpiration();
        } catch (JwtException e) {
            return null;
        }
    }
}
