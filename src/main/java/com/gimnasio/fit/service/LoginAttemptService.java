package com.gimnasio.fit.service;

import com.gimnasio.fit.entity.Usuario;
import com.gimnasio.fit.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Servicio para gestión de intentos de login fallidos y bloqueo de cuentas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Número máximo de intentos permitidos antes de bloquear.
     * Configurable desde application.properties
     */
    @Value("${app.security.max-login-attempts:5}")
    private int maxIntentos;

    /**
     * Duración del bloqueo temporal en horas.
     * Configurable desde application.properties
     */
    @Value("${app.security.lockout-duration-hours:24}")
    private int duracionBloqueoHoras;

    /**
     * Registra un intento de login fallido.
     * Si alcanza el máximo, bloquea la cuenta.
     * 
     * @param email Email del usuario
     */
    @Transactional
    public void registrarIntentoFallido(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        if (usuario == null) {
            log.debug("Intento de login fallido para email no registrado: {}", email);
            return;
        }

        // Incrementar contador
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);

        log.warn("Intento fallido #{} para usuario: {}", intentos, email);

        // Si alcanza el máximo, bloquear
        if (intentos >= maxIntentos) {
            usuario.setFechaBloqueo(Instant.now());
            usuario.setIntentosFallidos(0); // Resetear contador
            log.error("Usuario bloqueado por múltiples intentos fallidos: {}", email);
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Resetea el contador de intentos fallidos tras login exitoso.
     * 
     * @param usuario Usuario que logueó correctamente
     */
    @Transactional
    public void resetearIntentos(Usuario usuario) {
        if (usuario.getIntentosFallidos() > 0) {
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
            log.debug("Intentos fallidos reseteados para: {}", usuario.getEmail());
        }
    }

    /**
     * Verifica si una cuenta está bloqueada.
     * 
     * @param usuario Usuario a verificar
     * @return true si está bloqueado y el bloqueo sigue vigente
     */
    public boolean estaBloqueado(Usuario usuario) {
        if (usuario.getFechaBloqueo() == null) {
            return false;
        }

        // Verificar si el bloqueo temporal ya expiró (auto-desbloqueo)
        Instant finBloqueo = usuario.getFechaBloqueo().plus(Duration.ofHours(duracionBloqueoHoras));
        
        if (Instant.now().isAfter(finBloqueo)) {
            // Bloqueo expirado, auto-desbloquear
            desbloquear(usuario);
            return false;
        }

        return true;
    }

    /**
     * Desbloquea manualmente una cuenta (admin).
     * 
     * @param usuario Usuario a desbloquear
     */
    @Transactional
    public void desbloquear(Usuario usuario) {
        if (usuario.getFechaBloqueo() != null) {
            usuario.setFechaBloqueo(null);
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
            log.info("Usuario desbloqueado: {}", usuario.getEmail());
        }
    }

    /**
     * Obtiene el tiempo restante de bloqueo en minutos.
     * 
     * @param usuario Usuario bloqueado
     * @return Minutos restantes, o 0 si no está bloqueado
     */
    public long minutosRestantesBloqueo(Usuario usuario) {
        if (usuario.getFechaBloqueo() == null) {
            return 0;
        }

        Instant finBloqueo = usuario.getFechaBloqueo().plus(Duration.ofHours(duracionBloqueoHoras));
        Duration restante = Duration.between(Instant.now(), finBloqueo);

        return Math.max(0, restante.toMinutes());
    }
}
