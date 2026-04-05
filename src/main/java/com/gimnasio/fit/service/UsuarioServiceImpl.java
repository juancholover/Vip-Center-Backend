package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.entity.Rol;
import com.gimnasio.fit.entity.Usuario;
import com.gimnasio.fit.repository.RolRepository;
import com.gimnasio.fit.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Listar todos los usuarios
     */
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener usuario por ID
     */
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertirAResponse(usuario);
    }

    /**
     * Crear nuevo usuario
     */
    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        usuario.setActivo(true);
        usuario.setDebeCambiarPassword(true); // Debe cambiar en primer login

        // Asignar roles
        List<Rol> roles = rolRepository.findAllById(request.getRolesIds());
        if (roles.size() != request.getRolesIds().size()) {
            throw new RuntimeException("Uno o más roles no existen");
        }
        usuario.setRoles(roles);

        Usuario guardado = usuarioRepository.save(usuario);
        return convertirAResponse(guardado);
    }

    /**
     * Actualizar usuario existente
     */
    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar campos
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }

        if (request.getApellido() != null && !request.getApellido().isBlank()) {
            usuario.setApellido(request.getApellido());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(usuario.getEmail())) {
                if (usuarioRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("El email ya está en uso");
                }
                usuario.setEmail(request.getEmail());
            }
        }

        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }

        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        // Actualizar roles si se proporcionan
        if (request.getRolesIds() != null && !request.getRolesIds().isEmpty()) {
            List<Rol> roles = rolRepository.findAllById(request.getRolesIds());
            if (roles.size() != request.getRolesIds().size()) {
                throw new RuntimeException("Uno o más roles no existen");
            }
            usuario.setRoles(roles);
        }

        usuario.setFechaModificacion(Instant.now());
        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirAResponse(actualizado);
    }

    /**
     * Eliminar usuario (desactivar)
     */
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // No permitir eliminar el usuario actual
        // (Validar en controller con Principal)

        // Desactivar en lugar de eliminar físicamente
        usuario.setActivo(false);
        usuario.setFechaModificacion(Instant.now());
        usuarioRepository.save(usuario);
    }

    /**
     * Obtener perfil del usuario autenticado
     */
    public UsuarioResponse obtenerMiPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertirAResponse(usuario);
    }

    /**
     * Actualizar perfil del usuario autenticado
     */
    @Transactional
    public UsuarioResponse actualizarMiPerfil(String email, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }
        
        if (request.getApellido() != null && !request.getApellido().isBlank()) {
            usuario.setApellido(request.getApellido());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(usuario.getEmail())) {
                if (usuarioRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("El email ya está en uso");
                }
                usuario.setEmail(request.getEmail());
            }
        }

        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }

        usuario.setFechaModificacion(Instant.now());
        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirAResponse(actualizado);
    }

    /**
     * Cambiar contraseña del usuario autenticado
     */
    @Transactional
    public void cambiarMiPassword(String email, CambiarPasswordPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        // Validar que las nuevas coincidan
        if (!request.getPasswordNueva().equals(request.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas nuevas no coinciden");
        }

        // Validar que sea diferente
        if (passwordEncoder.matches(request.getPasswordNueva(), usuario.getPasswordHash())) {
            throw new RuntimeException("La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuario.setDebeCambiarPassword(false);
        usuario.setFechaModificacion(Instant.now());
        usuarioRepository.save(usuario);
    }

    /**
     * Obtener historial de acceso del usuario
     */
    public List<HistorialAccesoResponse> obtenerMiHistorial(String email) {
        // TODO: Implementar cuando se cree tabla historial_acceso
        // Por ahora retornar lista vacía
        return List.of();
    }

    /**
     * Convertir entidad a DTO de respuesta
     */
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .telefono(usuario.getTelefono())
                .activo(usuario.getActivo())
                .debeCambiarPassword(usuario.getDebeCambiarPassword())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaModificacion(usuario.getFechaModificacion())
                .roles(usuario.getUsuarioRoles().stream()
                        .map(ur -> RolDTO.builder()
                                .id(ur.getRol().getId().longValue())
                                .nombre(ur.getRol().getNombre())
                                .descripcion(ur.getRol().getDescripcion())
                                .build())
                        .toList())
                .build();
    }
}
