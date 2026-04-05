package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.service.RolServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Controller para gestión de roles (EPIC 2.5)
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RolController {

    private final RolServiceImpl rolService;

    /**
     * Listar todos los roles con sus permisos
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_roles.ver') or hasRole('ROLE_admin')")
    public ResponseEntity<List<RolResponse>> listar() {
        List<RolResponse> roles = rolService.obtenerTodos();
        if (roles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtener un rol por ID con sus permisos
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_roles.ver') or hasRole('ROLE_admin')")
    public ResponseEntity<RolResponse> obtenerPorId(@PathVariable Long id) {
        RolResponse rol = rolService.obtenerPorId(id);
        return ResponseEntity.ok(rol);
    }

    /**
     * Crear un nuevo rol
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PERM_roles.crear') or hasRole('ROLE_admin')")
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody CrearRolRequest request) {
        RolResponse rolCreado = rolService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/roles/" + rolCreado.getId()))
                .body(rolCreado);
    }

    /**
     * Actualizar un rol existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_roles.editar') or hasRole('ROLE_admin')")
    public ResponseEntity<RolResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRolRequest request) {
        RolResponse rolActualizado = rolService.actualizar(id, request);
        return ResponseEntity.ok(rolActualizado);
    }

    /**
     * Eliminar un rol
     * No permite eliminar el rol "Administrador"
     * No permite eliminar roles con usuarios asignados
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_roles.eliminar') or hasRole('ROLE_admin')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            rolService.eliminar(id);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Rol eliminado exitosamente",
                    "rolId", id
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error al eliminar rol",
                    "message", e.getMessage()
            ));
        }
    }
}