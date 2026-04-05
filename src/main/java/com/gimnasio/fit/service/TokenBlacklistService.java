package com.gimnasio.fit.service;

import com.gimnasio.fit.entity.TokenInvalido;
import com.gimnasio.fit.entity.Usuario;
import com.gimnasio.fit.repository.TokenInvalidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Servicio para gestión de blacklist de tokens JWT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final TokenInvalidoRepository tokenInvalidoRepository;

    /**
     * Invalida un token JWT (agregar a blacklist).
     * 
     * @param token Token JWT completo
     * @param expiresAt Fecha de expiración natural del token
     * @param motivo Razón de invalidación
     * @param usuario Usuario propietario del token
     */
    @Transactional
    public void invalidarToken(String token, Instant expiresAt, String motivo, Usuario usuario) {
        String hash = calcularHash(token);
        
        // Verificar si ya está invalidado (idempotencia)
        if (tokenInvalidoRepository.existsByTokenHash(hash)) {
            log.debug("Token ya estaba invalidado: {}", hash);
            return;
        }

        TokenInvalido tokenInvalido = new TokenInvalido();
        tokenInvalido.setTokenHash(hash);
        tokenInvalido.setFechaExpiracion(expiresAt);
        tokenInvalido.setMotivo(motivo);
        tokenInvalido.setUsuario(usuario);

        tokenInvalidoRepository.save(tokenInvalido);
        log.info("Token invalidado exitosamente. Motivo: {}, Usuario: {}", motivo, usuario.getEmail());
    }

    /**
     * Verifica si un token está en la blacklist.
     * 
     * @param token Token JWT completo
     * @return true si el token está invalidado
     */
    public boolean estaInvalidado(String token) {
        String hash = calcularHash(token);
        return tokenInvalidoRepository.existsByTokenHash(hash);
    }

    /**
     * Invalida todos los tokens activos de un usuario.
     * Útil cuando admin necesita cerrar todas las sesiones.
     * 
     * @param usuario Usuario cuyas sesiones se cerrarán
     * @param motivo Razón de la invalidación masiva
     */
    @Transactional
    public void invalidarTodosLosTokensDeUsuario(Usuario usuario, String motivo) {
        // Nota: Este método requeriría almacenar todos los tokens activos
        // Por simplicidad, aquí solo registramos el evento
        // En producción, podrías usar Redis o similar para tracking
        log.warn("Invalidación masiva solicitada para usuario: {}, Motivo: {}", usuario.getEmail(), motivo);
        // TODO: Implementar si se requiere tracking de tokens activos
    }

    /**
     * Limpieza automática de tokens expirados.
     * Se ejecuta cada hora.
     */
    @Scheduled(fixedRate = 3600000) // 1 hora = 3600000 ms
    @Transactional
    public void limpiarTokensExpirados() {
        Instant ahora = Instant.now();
        int eliminados = tokenInvalidoRepository.deleteByFechaExpiracionBefore(ahora);
        
        if (eliminados > 0) {
            log.info("Limpieza automática: {} tokens expirados eliminados", eliminados);
        }
    }

    /**
     * Calcula hash SHA-256 de un token.
     * 
     * @param token Token completo
     * @return Hash hexadecimal (64 caracteres)
     */
    private String calcularHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculando hash SHA-256", e);
        }
    }
}
