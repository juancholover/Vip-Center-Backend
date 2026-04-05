package com.gimnasio.fit.config;

import com.gimnasio.fit.service.TokenBlacklistService;
import com.gimnasio.fit.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT:
 * - Se ejecuta una sola vez por request.
 * - Extrae, valida y decodifica el Access Token.
 * - Verifica si está invalidado (logout).
 * - Carga el usuario autenticado en el contexto de seguridad.
 *
 * Compatible con el flujo de Refresh Token y la Blacklist.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = extractToken(header);

        if (token != null) {
            try {
                // 1️⃣ Verificar si está invalidado por logout
                if (tokenBlacklistService.estaInvalidado(token)) {
                    request.setAttribute("jwt_error", "Token invalidado (logout realizado)");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 2️⃣ Verificar expiración
                if (jwtUtil.isTokenExpired(token)) {
                    request.setAttribute("jwt_error", "Token expirado");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 3️⃣ Validar firma y obtener usuario
                Jws<Claims> jws = jwtUtil.parseToken(token);
                String username = jws.getPayload().getSubject();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 4️⃣ Crear autenticación e inyectarla al contexto
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (JwtException ex) {
                // Firma inválida o token manipulado
                request.setAttribute("jwt_error", "Token inválido o manipulado: " + ex.getMessage());
            } catch (Exception ex) {
                // Otros errores inesperados
                request.setAttribute("jwt_error", "Error al procesar token: " + ex.getMessage());
            }
        }

        // Continuar con la cadena
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * Ejemplo de formato esperado: "Bearer eyJhbGciOiJIUzI1..."
     */
    private String extractToken(String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * Excluye endpoints públicos (login y refresh).
     * /api/auth/me sigue requiriendo JWT.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/refresh") ||
                path.startsWith("/swagger") || // opcional si usas swagger
                path.startsWith("/v3/api-docs");
    }
}
