package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.CrearMembresiaRequest;
import com.gimnasio.fit.dto.MembresiaDTO;
import com.gimnasio.fit.service.MembresiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de membresías.
 */
@RestController
@RequestMapping("/api/membresias")
@RequiredArgsConstructor
@Slf4j
public class MembresiaController {

    private final MembresiaService membresiaService;

    /**
     * 🌐 PÚBLICO - Listar membresías activas (para página de ventas).
     */
    @GetMapping("/activas")
    public ResponseEntity<List<MembresiaDTO>> listarActivas() {
        log.info("📋 Listando membresías activas");
        List<MembresiaDTO> membresias = membresiaService.listarActivas();
        return ResponseEntity.ok(membresias);
    }

    /**
     * 🔒 PROTEGIDO - Listar todas las membresías (incluye inactivas).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<MembresiaDTO>> listarTodas() {
        log.info("📋 Listando todas las membresías");
        List<MembresiaDTO> membresias = membresiaService.listarTodas();
        return ResponseEntity.ok(membresias);
    }

    /**
     * 🔒 PROTEGIDO - Obtener membresía por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        log.info("🔍 Consultando membresía ID: {}", id);
        try {
            MembresiaDTO membresia = membresiaService.obtenerPorId(id);
            return ResponseEntity.ok(membresia);
        } catch (Exception e) {
            log.error("❌ Error al obtener membresía: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔒 PROTEGIDO - Crear nueva membresía (solo ADMIN).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody CrearMembresiaRequest request) {
        log.info("➕ Creando nueva membresía: {}", request.getNombre());
        try {
            MembresiaDTO membresia = membresiaService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(membresia);
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error al crear membresía: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al crear membresía"));
        }
    }

    /**
     * 🔒 PROTEGIDO - Actualizar membresía existente (solo ADMIN).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CrearMembresiaRequest request
    ) {
        log.info("✏️ Actualizando membresía ID: {}", id);
        try {
            MembresiaDTO membresia = membresiaService.actualizar(id, request);
            return ResponseEntity.ok(membresia);
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error al actualizar membresía: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔒 PROTEGIDO - Activar/Desactivar membresía (solo ADMIN).
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado
    ) {
        log.info("🔄 Cambiando estado de membresía ID {} a: {}", id, estado);
        try {
            MembresiaDTO membresia = membresiaService.cambiarEstado(id, estado);
            return ResponseEntity.ok(membresia);
        } catch (Exception e) {
            log.error("❌ Error al cambiar estado: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🔒 PROTEGIDO - Recalcular orden de membresías (solo ADMIN).
     */
    @PostMapping("/recalcular-orden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recalcularOrden() {
        log.info("🔄 Recalculando orden de membresías");
        try {
            membresiaService.recalcularOrden();
            return ResponseEntity.ok(Map.of("mensaje", "Orden recalculado exitosamente"));
        } catch (Exception e) {
            log.error("❌ Error al recalcular orden: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al recalcular orden"));
        }
    }

    /**
     * 🔒 PROTEGIDO - Eliminar membresía (soft delete - solo ADMIN).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        log.info("🗑️ Eliminando membresía ID: {}", id);
        try {
            membresiaService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Membresía desactivada exitosamente"));
        } catch (Exception e) {
            log.error("❌ Error al eliminar membresía: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
