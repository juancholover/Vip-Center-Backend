package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.AsistenciaDTO;
import com.gimnasio.fit.dto.RegistrarAsistenciaManualRequest;
import com.gimnasio.fit.dto.RegistrarAsistenciaResponse;
import com.gimnasio.fit.service.AsistenciaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de asistencias.
 */
@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
@Slf4j
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final com.gimnasio.fit.utils.SimpleRateLimiter rateLimiter;

    /**
     * ⚡ ENDPOINT PÚBLICO - Registrar asistencia por QR.
     * Este endpoint NO requiere autenticación JWT.
     * Se usa en la aplicación móvil/web para escaneo de QR.
     *
     * @param token Token QR del cliente
     * @param latitud Coordenada GPS (opcional)
     * @param longitud Coordenada GPS (opcional)
     * @param request HttpServletRequest para extraer User-Agent e IP
     * @return Respuesta con información de la asistencia
     */
    @PostMapping("/registrar")
    public ResponseEntity<RegistrarAsistenciaResponse> registrarAsistencia(
            @RequestParam String token,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            HttpServletRequest request
    ) {
        log.info("📱 Solicitud de registro de asistencia con token QR");

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = obtenerIpCliente(request);

    // Rate limit sencillo por (token+IP) para evitar spam/doble click
    String rlKey = token + "|" + ipAddress;
    if (!rateLimiter.allow(rlKey)) {
        log.warn("⏱️ Too Many Requests para clave {}", rlKey);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(RegistrarAsistenciaResponse.builder()
                .success(false)
                .mensaje("⏱️ Intenta nuevamente en unos segundos")
                .build());
    }

        try {
            RegistrarAsistenciaResponse response = asistenciaService.registrarPorToken(
                    token,
                    userAgent,
                    ipAddress,
                    latitud,
                    longitud
            );

            if (response.isSuccess()) {
                log.info("✅ Asistencia registrada exitosamente para cliente ID: {}", 
                        response.getCliente().getId());
                return ResponseEntity.ok(response);
            } else {
                // Cliente ya registró hoy o no cumple requisitos
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("❌ Token QR inválido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RegistrarAsistenciaResponse.builder()
                            .success(false)
                            .mensaje("❌ Código QR no válido o no existe")
                            .build());
        } catch (Exception e) {
            log.error("❌ Error inesperado al registrar asistencia: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegistrarAsistenciaResponse.builder()
                            .success(false)
                            .mensaje("❌ Error del servidor. Por favor intente nuevamente.")
                            .build());
        }
    }

    /**
     * 🔒 PROTEGIDO - Registrar asistencia manualmente (solo personal autorizado).
     *
     * @param request DTO con clienteId y notas
     * @return Respuesta con información de la asistencia
     */
    @PostMapping("/registrar-manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> registrarManual(
            @Valid @RequestBody RegistrarAsistenciaManualRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("🧑‍💼 Registro manual de asistencia para cliente ID: {}", request.getClienteId());

        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = obtenerIpCliente(httpRequest);

        try {
            RegistrarAsistenciaResponse response = asistenciaService.registrarManual(
                    request.getClienteId(),
                    request.getNotas(),
                    userAgent,
                    ipAddress
            );

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Error al registrar asistencia manual: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔒 PROTEGIDO - Obtener asistencias del día actual.
     *
     * @return Lista de asistencias de hoy
     */
    @GetMapping("/hoy")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Map<String, Object>> obtenerAsistenciasHoy() {
        log.info("📊 Consultando asistencias de hoy");
        
        List<AsistenciaDTO> asistencias = asistenciaService.obtenerAsistenciasHoy();
        
        Map<String, Object> response = new HashMap<>();
        response.put("fecha", LocalDate.now());
        response.put("total", asistencias.size());
        response.put("asistencias", asistencias);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 🔒 PROTEGIDO - Obtener asistencias por rango de fechas.
     *
     * @param inicio Fecha de inicio (formato: yyyy-MM-dd)
     * @param fin Fecha de fin (formato: yyyy-MM-dd)
     * @return Lista de asistencias en el rango
     */
    @GetMapping("/rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Map<String, Object>> obtenerAsistenciasPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("📊 Consultando asistencias desde {} hasta {}", inicio, fin);
        
        if (inicio.isAfter(fin)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La fecha de inicio no puede ser posterior a la fecha de fin"));
        }
        
        List<AsistenciaDTO> asistencias = asistenciaService.obtenerAsistenciasPorRango(inicio, fin);
        
        Map<String, Object> response = new HashMap<>();
        response.put("inicio", inicio);
        response.put("fin", fin);
        response.put("total", asistencias.size());
        response.put("asistencias", asistencias);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 🔒 PROTEGIDO - Obtener historial de asistencias de un cliente.
     *
     * @param clienteId ID del cliente
     * @return Lista de asistencias del cliente
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> obtenerHistorialCliente(@PathVariable Long clienteId) {
        log.info("📊 Consultando historial de asistencias del cliente ID: {}", clienteId);
        
        try {
            List<AsistenciaDTO> asistencias = asistenciaService.obtenerHistorialCliente(clienteId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("clienteId", clienteId);
            response.put("totalAsistencias", asistencias.size());
            response.put("asistencias", asistencias);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener historial: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔒 PROTEGIDO - Verificar si un cliente ya registró asistencia hoy.
     *
     * @param clienteId ID del cliente
     * @return true si ya registró, false si no
     */
    @GetMapping("/verificar/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Map<String, Object>> verificarAsistenciaHoy(@PathVariable Long clienteId) {
        log.info("🔍 Verificando asistencia hoy del cliente ID: {}", clienteId);
        
        boolean registrado = asistenciaService.verificarAsistenciaHoy(clienteId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("clienteId", clienteId);
        response.put("fecha", LocalDate.now());
        response.put("registrado", registrado);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 🔒 PROTEGIDO - Contar asistencias de un cliente en un mes específico.
     *
     * @param clienteId ID del cliente
     * @param anio Año
     * @param mes Mes (1-12)
     * @return Cantidad de asistencias
     */
    @GetMapping("/estadisticas/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> contarAsistenciasMes(
            @PathVariable Long clienteId,
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        log.info("📊 Contando asistencias del cliente {} en {}/{}", clienteId, mes, anio);
        
        try {
            Long count = asistenciaService.contarAsistenciasMes(clienteId, anio, mes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("clienteId", clienteId);
            response.put("anio", anio);
            response.put("mes", mes);
            response.put("totalAsistencias", count);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔒 PROTEGIDO - Eliminar una asistencia.
     *
     * @param id ID de la asistencia a eliminar
     * @return Mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Map<String, String>> eliminarAsistencia(@PathVariable Long id) {
        log.info("🗑️ Eliminando asistencia ID: {}", id);
        
        try {
            asistenciaService.eliminarAsistencia(id);
            return ResponseEntity.ok(Map.of("message", "Asistencia eliminada correctamente"));
        } catch (EntityNotFoundException e) {
            log.error("❌ Asistencia no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Asistencia no encontrada"));
        } catch (Exception e) {
            log.error("❌ Error al eliminar asistencia: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔒 PROTEGIDO - Eliminar todas las asistencias de HOY para un cliente.
     */
    @DeleteMapping("/cliente/{clienteId}/hoy")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Map<String, Object>> eliminarAsistenciasDeHoy(@PathVariable Long clienteId) {
        log.info("🗑️ Eliminando asistencias de HOY para cliente ID: {}", clienteId);
        int eliminadas = asistenciaService.eliminarAsistenciasDeHoy(clienteId);
        return ResponseEntity.ok(Map.of(
                "clienteId", clienteId,
                "eliminadas", eliminadas,
                "fecha", LocalDate.now()
        ));
    }

    // ========== Método auxiliar ==========

    /**
     * Extrae la IP real del cliente, considerando proxies y balanceadores.
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs en X-Forwarded-For, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
