package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.ActualizarUsuarioRequest;
import com.gimnasio.fit.dto.CrearUsuarioRequest;
import com.gimnasio.fit.dto.RolDTO;
import com.gimnasio.fit.dto.UsuarioResponse;
import com.gimnasio.fit.entity.Usuario;
import com.gimnasio.fit.repository.UsuarioRepository;
import com.gimnasio.fit.repository.UsuarioRolRepository;
import com.gimnasio.fit.service.LoginAttemptService;
import com.gimnasio.fit.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final LoginAttemptService loginAttemptService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_usuarios.crear') or hasRole('ROLE_admin')")
    @Transactional
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest req){
        Usuario u = new Usuario();
        u.setEmail(req.getEmail());
        u.setPasswordHash(req.getPassword());
        u.setNombre(req.getNombre());
        u.setApellido(req.getApellido());
        u.setTelefono(req.getTelefono());
        u.setFechaModificacion(java.time.Instant.now());
        
        Usuario guardado = usuarioService.crear(u);

        // Asignar roles al usuario recién creado
        if (req.getRolesIds() != null && !req.getRolesIds().isEmpty()) {
            for (Long rolId : req.getRolesIds()) {
                usuarioService.asignarRol(guardado.getId().intValue(), rolId.intValue(), null);
            }
            // Recargar el usuario para obtener los roles asignados
            guardado = usuarioRepository.findById(guardado.getId()).orElseThrow();
        }

        UsuarioResponse resp = UsuarioResponse.builder()
                .id(guardado.getId())
                .email(guardado.getEmail())
                .nombre(guardado.getNombre())
                .apellido(guardado.getApellido())
                .telefono(guardado.getTelefono())
                .activo(guardado.getActivo())
                .debeCambiarPassword(guardado.getDebeCambiarPassword())
                .fechaBloqueo(guardado.getFechaBloqueo())
                .fechaCreacion(guardado.getFechaCreacion())
                .fechaModificacion(guardado.getFechaModificacion())
                .roles(guardado.getUsuarioRoles().stream()
                        .map(ur -> RolDTO.builder()
                                .id(ur.getRol().getId().longValue())
                                .nombre(ur.getRol().getNombre())
                                .descripcion(ur.getRol().getDescripcion())
                                .build())
                        .toList())
                .build();

        return ResponseEntity.created(URI.create("/api/usuarios/" + guardado.getId())).body(resp);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(){
        List<UsuarioResponse> lista = usuarioService.listar().stream().map(u ->
                UsuarioResponse.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .nombre(u.getNombre())
                        .apellido(u.getApellido())
                        .telefono(u.getTelefono())
                        .activo(u.getActivo())
                        .debeCambiarPassword(u.getDebeCambiarPassword())
                        .fechaBloqueo(u.getFechaBloqueo())
                        .fechaCreacion(u.getFechaCreacion())
                        .fechaModificacion(u.getFechaModificacion())
                        .roles(u.getUsuarioRoles().stream()
                                .map(ur -> RolDTO.builder()
                                        .id(ur.getRol().getId().longValue())
                                        .nombre(ur.getRol().getNombre())
                                        .descripcion(ur.getRol().getDescripcion())
                                        .build())
                                .toList())
                        .build()
        ).toList();
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{usuarioId}/roles/{rolId}")
    public ResponseEntity<?> asignarRol(@PathVariable Integer usuarioId,
                                        @PathVariable Integer rolId,
                                        @RequestParam(name="asignadoPor", required=false) Integer asignadoPor){
        usuarioService.asignarRol(usuarioId, rolId, asignadoPor);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener un usuario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_usuarios.ver') or hasRole('ROLE_admin')")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        UsuarioResponse resp = UsuarioResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .telefono(u.getTelefono())
                .activo(u.getActivo())
                .debeCambiarPassword(u.getDebeCambiarPassword())
                .fechaBloqueo(u.getFechaBloqueo())
                .fechaCreacion(u.getFechaCreacion())
                .fechaModificacion(u.getFechaModificacion())
                .roles(u.getUsuarioRoles().stream()
                        .map(ur -> RolDTO.builder()
                                .id(ur.getRol().getId().longValue())
                                .nombre(ur.getRol().getNombre())
                                .descripcion(ur.getRol().getDescripcion())
                                .build())
                        .toList())
                .build();
        
        return ResponseEntity.ok(resp);
    }

    /**
     * Actualizar un usuario existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_usuarios.editar') or hasRole('ROLE_admin')")
    @Transactional
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest req) {
        
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Actualizar campos
        if (req.getEmail() != null && !req.getEmail().equals(u.getEmail())) {
            // Verificar que el nuevo email no exista
            if (usuarioRepository.findByEmail(req.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            u.setEmail(req.getEmail());
        }
        
        if (req.getNombre() != null && !req.getNombre().isBlank()) {
            u.setNombre(req.getNombre());
        }
        
        if (req.getApellido() != null && !req.getApellido().isBlank()) {
            u.setApellido(req.getApellido());
        }
        
        if (req.getTelefono() != null) {
            u.setTelefono(req.getTelefono());
        }
        
        if (req.getActivo() != null) {
            u.setActivo(req.getActivo());
        }
        
        u.setFechaModificacion(java.time.Instant.now());
        Usuario actualizado = usuarioRepository.save(u);
        
        // Actualizar roles si se proporcionan
        if (req.getRolesIds() != null) {
            // Eliminar roles anteriores
            usuarioRolRepository.deleteByUsuarioId(id);
            usuarioRolRepository.flush();
            
            // Asignar nuevos roles
            for (Long rolId : req.getRolesIds()) {
                usuarioService.asignarRol(id.intValue(), rolId.intValue(), null);
            }
            
            // Recargar el usuario para obtener los roles actualizados
            actualizado = usuarioRepository.findById(id).orElseThrow();
        }
        
        UsuarioResponse resp = UsuarioResponse.builder()
                .id(actualizado.getId())
                .email(actualizado.getEmail())
                .nombre(actualizado.getNombre())
                .apellido(actualizado.getApellido())
                .telefono(actualizado.getTelefono())
                .activo(actualizado.getActivo())
                .debeCambiarPassword(actualizado.getDebeCambiarPassword())
                .fechaBloqueo(actualizado.getFechaBloqueo())
                .fechaCreacion(actualizado.getFechaCreacion())
                .fechaModificacion(actualizado.getFechaModificacion())
                .roles(actualizado.getUsuarioRoles().stream()
                        .map(ur -> RolDTO.builder()
                                .id(ur.getRol().getId().longValue())
                                .nombre(ur.getRol().getNombre())
                                .descripcion(ur.getRol().getDescripcion())
                                .build())
                        .toList())
                .build();
        
        return ResponseEntity.ok(resp);
    }

    /**
     * Eliminar permanentemente un usuario
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_usuarios.eliminar') or hasRole('ROLE_admin')")
    @Transactional
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Primero eliminar los roles del usuario
        usuarioRolRepository.deleteByUsuarioId(id);
        usuarioRolRepository.flush();
        
        // Luego eliminar el usuario
        usuarioRepository.delete(u);
        
        return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario eliminado exitosamente",
                "usuarioId", id
        ));
    }

    /**
     * Desbloquea un usuario que fue bloqueado por intentos fallidos.
     * Solo accesible por administradores.
     */
    @PostMapping("/{usuarioId}/desbloquear")
    @PreAuthorize("hasAuthority('PERM_usuarios.editar') or hasRole('ROLE_admin')")
    public ResponseEntity<?> desbloquear(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFechaBloqueo() == null) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "El usuario no está bloqueado"));
        }

        loginAttemptService.desbloquear(usuario);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario desbloqueado exitosamente",
                "usuarioId", usuarioId,
                "email", usuario.getEmail()
        ));
    }
}