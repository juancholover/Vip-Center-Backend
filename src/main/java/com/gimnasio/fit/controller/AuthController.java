package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.entity.Usuario;
import com.gimnasio.fit.repository.UsuarioRepository;
import com.gimnasio.fit.service.HistorialAccesoService;
import com.gimnasio.fit.service.LoginAttemptService;
import com.gimnasio.fit.service.TokenBlacklistService;
import com.gimnasio.fit.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador de autenticación y gestión de sesión.
 * Incluye:
 *  - Login
 *  - Refresh de tokens
 *  - Logout (blacklist)
 *  - Gestión de contraseñas
 *  - Perfil de usuario autenticado
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HistorialAccesoService historialAccesoService;

    // =====================================================
    // 🔐 LOGIN
    // =====================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        Usuario usuario = usuarioRepository.fetchWithRolesAndPermisos(request.getEmail()).orElse(null);
        if (usuario == null)
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));

        // ❌ BLOQUEO DE CUENTAS DESACTIVADO ❌
        // Validar bloqueo
        // if (loginAttemptService.estaBloqueado(usuario)) {
        //     long minutos = loginAttemptService.minutosRestantesBloqueo(usuario);
        //     return ResponseEntity.status(403).body(Map.of(
        //             "error", "Cuenta bloqueada por múltiples intentos fallidos",
        //             "minutosRestantes", minutos,
        //             "mensaje", "Intente nuevamente en " + minutos + " minutos"
        //     ));
        // }

        if (!usuario.getActivo())
            return ResponseEntity.status(403).body(Map.of("error", "Cuenta desactivada. Contacte al administrador."));

        try {
            // Autenticar credenciales
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Reset intentos fallidos y actualizar último acceso
            loginAttemptService.resetearIntentos(usuario);
            usuario.setUltimoAcceso(Instant.now());
            usuarioRepository.save(usuario);
            
            // Registrar login exitoso en historial
            String ip = obtenerIpCliente(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            historialAccesoService.registrarLoginExitoso(usuario.getId(), usuario.getEmail(), ip, userAgent);

            // Roles y permisos
            Set<String> roles = usuario.getUsuarioRoles().stream()
                    .map(ur -> ur.getRol().getNombre()).collect(Collectors.toSet());
            Set<String> permisos = usuario.getUsuarioRoles().stream()
                    .flatMap(ur -> ur.getRol().getRolPermisos().stream())
                    .map(rp -> rp.getPermiso().getCodigo()).collect(Collectors.toSet());

            Map<String, Object> claims = Map.of("roles", roles, "permisos", permisos);

            // Generar tokens
            String access = jwtUtil.generateAccessToken(usuario.getEmail(), claims);
            String refresh = jwtUtil.generateRefreshToken(usuario.getEmail());

            // Respuesta
            AuthResponse resp = AuthResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .userId(usuario.getId())
                    .email(usuario.getEmail())
                    .nombre(usuario.getNombre())
                    .apellido(usuario.getApellido())
                    .telefono(usuario.getTelefono())
                    .roles(roles)
                    .permisos(permisos)
                    .debeCambiarPassword(usuario.getDebeCambiarPassword())
                    .build();

            return ResponseEntity.ok(resp);

        } catch (BadCredentialsException e) {
            // ❌ REGISTRO DE INTENTOS FALLIDOS DESACTIVADO ❌
            // loginAttemptService.registrarIntentoFallido(request.getEmail());
            
            // Registrar intento fallido en historial
            String ip = obtenerIpCliente(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            historialAccesoService.registrarLoginFallido(request.getEmail(), ip, userAgent, "Credenciales incorrectas");
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        } catch (LockedException e) {
            return ResponseEntity.status(403).body(Map.of("error", "Cuenta bloqueada"));
        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(Map.of("error", "Cuenta desactivada"));
        }
    }

    // =====================================================
    // ♻️ REFRESH TOKEN
    // =====================================================
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest req) {
        try {
            var jws = jwtUtil.parseToken(req.getRefreshToken());

            if (!jwtUtil.isRefreshToken(jws)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "invalid_token",
                        "message", "El token proporcionado no es un refresh token válido."
                ));
            }

            // Validar expiración manual
            Date expiration = jws.getPayload().getExpiration();
            if (expiration.before(new Date())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "session_expired",
                        "message", "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
                ));
            }

            String email = jws.getPayload().getSubject();
            Usuario usuario = usuarioRepository.fetchWithRolesAndPermisos(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Set<String> roles = usuario.getUsuarioRoles().stream()
                    .map(ur -> ur.getRol().getNombre()).collect(Collectors.toSet());
            Set<String> permisos = usuario.getUsuarioRoles().stream()
                    .flatMap(ur -> ur.getRol().getRolPermisos().stream())
                    .map(rp -> rp.getPermiso().getCodigo()).collect(Collectors.toSet());

            Map<String, Object> claims = Map.of("roles", roles, "permisos", permisos);

            String newAccess = jwtUtil.generateAccessToken(email, claims);
            String newRefresh = jwtUtil.generateRefreshToken(email);

            AuthResponse resp = AuthResponse.builder()
                    .accessToken(newAccess)
                    .refreshToken(newRefresh)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .userId(usuario.getId())
                    .email(usuario.getEmail())
                    .nombre(usuario.getNombre())
                    .apellido(usuario.getApellido())
                    .telefono(usuario.getTelefono())
                    .roles(roles)
                    .permisos(permisos)
                    .build();

            return ResponseEntity.ok(resp);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "invalid_token",
                    "message", "El refresh token no es válido o fue manipulado."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "refresh_failed",
                    "message", "No se pudo refrescar la sesión. Intente iniciar sesión nuevamente."
            ));
        }
    }

    // =====================================================
    // 👤 PERFIL (USUARIO ACTUAL)
    // =====================================================
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud))
            return ResponseEntity.status(401).build();

        Usuario u = usuarioRepository.fetchWithRolesAndPermisos(ud.getUsername()).orElseThrow();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", u.getId());
        body.put("email", u.getEmail());
        body.put("nombre", u.getNombre());
        body.put("apellido", u.getApellido());
        body.put("debeCambiarPassword", u.getDebeCambiarPassword());
        body.put("roles", u.getUsuarioRoles().stream().map(ur -> ur.getRol().getNombre()).toList());
        body.put("permisos", u.getUsuarioRoles().stream()
                .flatMap(ur -> ur.getRol().getRolPermisos().stream())
                .map(rp -> rp.getPermiso().getCodigo()).toList());

        return ResponseEntity.ok(body);
    }

    // =====================================================
    // 🔑 CONTRASEÑAS (CAMBIO / ACTUALIZACIÓN)
    // =====================================================
    @PostMapping("/change-password")
    public ResponseEntity<?> cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request, HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud))
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));

        if (!request.getNuevaPassword().equals(request.getConfirmarPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));

        Usuario usuario = usuarioRepository.findByEmail(ud.getUsername()).orElseThrow();
        if (!usuario.getDebeCambiarPassword())
            return ResponseEntity.badRequest().body(Map.of("error", "No es necesario cambiar la contraseña"));

        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);
        
        // Registrar cambio de contraseña en historial
        String ip = obtenerIpCliente(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        historialAccesoService.registrarCambioPassword(usuario.getId(), usuario.getEmail(), ip, userAgent);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> actualizarPassword(@Valid @RequestBody ActualizarPasswordRequest request, HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud))
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));

        if (!request.getNuevaPassword().equals(request.getConfirmarPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas nuevas no coinciden"));

        Usuario usuario = usuarioRepository.findByEmail(ud.getUsername()).orElseThrow();
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash()))
            return ResponseEntity.badRequest().body(Map.of("error", "Contraseña actual incorrecta"));
        if (passwordEncoder.matches(request.getNuevaPassword(), usuario.getPasswordHash()))
            return ResponseEntity.badRequest().body(Map.of("error", "La nueva contraseña debe ser diferente a la actual"));

        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
        
        // Registrar cambio de contraseña en historial
        String ip = obtenerIpCliente(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        historialAccesoService.registrarCambioPassword(usuario.getId(), usuario.getEmail(), ip, userAgent);
        
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }

    // =====================================================
    // 🚪 LOGOUT
    // =====================================================
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.badRequest().body(null);

        String token = authHeader.substring(7);

        try {
            var jws = jwtUtil.parseToken(token);
            String email = jws.getPayload().getSubject();
            Instant exp = jws.getPayload().getExpiration().toInstant();
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            tokenBlacklistService.invalidarToken(token, exp, "logout", usuario);
            
            // Registrar logout en historial
            if (usuario != null) {
                String ip = obtenerIpCliente(httpRequest);
                String userAgent = httpRequest.getHeader("User-Agent");
                historialAccesoService.registrarLogout(usuario.getId(), usuario.getEmail(), ip, userAgent);
            }
            
            return ResponseEntity.ok(LogoutResponse.success());
        } catch (Exception e) {
            return ResponseEntity.ok(LogoutResponse.success());
        }
    }

    // =====================================================
    // 👤 PERFIL TEMPORAL (EPIC 2.5)
    // =====================================================
    @GetMapping("/usuarios/me")
    public ResponseEntity<?> obtenerMiPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud))
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));

        Usuario usuario = usuarioRepository.fetchWithRolesAndPermisos(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", usuario.getId());
        resp.put("email", usuario.getEmail());
        resp.put("nombre", usuario.getNombre());
        resp.put("apellido", usuario.getApellido());
        resp.put("telefono", usuario.getTelefono());
        resp.put("activo", usuario.getActivo());
        resp.put("debeCambiarPassword", usuario.getDebeCambiarPassword());
        resp.put("fechaCreacion", usuario.getFechaCreacion());
        resp.put("roles", usuario.getUsuarioRoles().stream()
                .map(ur -> ur.getRol().getNombre()).toList());

        return ResponseEntity.ok(resp);
    }

    @PutMapping("/usuarios/me")
    public ResponseEntity<?> actualizarMiPerfil(@Valid @RequestBody ActualizarPerfilRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud))
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));

        Usuario usuario = usuarioRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (req.getNombre() != null && !req.getNombre().isBlank()) usuario.setNombre(req.getNombre());
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            usuarioRepository.findByEmail(req.getEmail()).ifPresent(u -> {
                if (!u.getId().equals(usuario.getId()))
                    throw new RuntimeException("El email ya está en uso");
            });
            usuario.setEmail(req.getEmail());
        }
        if (req.getTelefono() != null) usuario.setTelefono(req.getTelefono());

        usuarioRepository.save(usuario);

        Map<String, Object> resp = Map.of(
                "id", usuario.getId(),
                "email", usuario.getEmail(),
                "nombre", usuario.getNombre(),
                "apellido", usuario.getApellido(),
                "telefono", usuario.getTelefono(),
                "activo", usuario.getActivo(),
                "debeCambiarPassword", usuario.getDebeCambiarPassword(),
                "fechaCreacion", usuario.getFechaCreacion()
        );

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/usuarios/me/cambiar-password")
    public ResponseEntity<?> cambiarMiPasswordPerfil(@Valid @RequestBody CambiarPasswordPerfilRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails ud))
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));

        if (!req.getPasswordNueva().equals(req.getConfirmarPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas nuevas no coinciden"));

        Usuario usuario = usuarioRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(req.getPasswordActual(), usuario.getPasswordHash()))
            return ResponseEntity.badRequest().body(Map.of("error", "Contraseña actual incorrecta"));
        if (passwordEncoder.matches(req.getPasswordNueva(), usuario.getPasswordHash()))
            return ResponseEntity.badRequest().body(Map.of("error", "La nueva contraseña debe ser diferente a la actual"));

        usuario.setPasswordHash(passwordEncoder.encode(req.getPasswordNueva()));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }

    @GetMapping("/usuarios/me/historial")
    public ResponseEntity<?> obtenerMiHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails))
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return ResponseEntity.ok(historialAccesoService.obtenerHistorialPorUsername(
            userDetails.getUsername(), page, size));
    }
    
    // =====================================================
    // 🛠️ MÉTODOS AUXILIARES
    // =====================================================
    
    /**
     * Obtiene la IP real del cliente considerando proxies y balanceadores
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Si hay múltiples IPs (proxies en cadena), tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
