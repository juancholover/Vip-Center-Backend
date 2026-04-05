package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.entity.Permiso;
import com.gimnasio.fit.entity.Rol;
import com.gimnasio.fit.repository.PermisoRepository;
import com.gimnasio.fit.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolServiceImpl {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    /**
     * Listar todos los roles
     */
    public List<RolResponse> obtenerTodos() {
        return rolRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener rol por ID
     */
    public RolResponse obtenerPorId(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        return convertirAResponse(rol);
    }

    /**
     * Crear nuevo rol
     */
    @Transactional
    public RolResponse crear(CrearRolRequest request) {
        // Validar que el nombre no exista
        if (rolRepository.findByNombre(request.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un rol con ese nombre");
        }

        Rol rol = new Rol();
        rol.setNombre(request.getNombre());
        rol.setDescripcion(request.getDescripcion());
        rol.setActivo(true);

        // Asignar permisos
        List<Permiso> permisos = permisoRepository.findAllById(request.getPermisosIds());
        if (permisos.size() != request.getPermisosIds().size()) {
            throw new RuntimeException("Uno o más permisos no existen");
        }
        rol.setPermisos(permisos);

        Rol guardado = rolRepository.save(rol);
        return convertirAResponse(guardado);
    }

    /**
     * Actualizar rol existente
     */
    @Transactional
    public RolResponse actualizar(Long id, ActualizarRolRequest request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // No permitir modificar rol Administrador
        if (rol.getNombre().equals("Administrador")) {
            throw new RuntimeException("No se puede modificar el rol Administrador");
        }

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            // Validar que el nuevo nombre no exista
            rolRepository.findByNombre(request.getNombre())
                    .ifPresent(r -> {
                        if (!r.getId().equals(id)) {
                            throw new RuntimeException("Ya existe otro rol con ese nombre");
                        }
                    });
            rol.setNombre(request.getNombre());
        }

        if (request.getDescripcion() != null) {
            rol.setDescripcion(request.getDescripcion());
        }

        if (request.getPermisosIds() != null && !request.getPermisosIds().isEmpty()) {
            List<Permiso> permisos = permisoRepository.findAllById(request.getPermisosIds());
            if (permisos.size() != request.getPermisosIds().size()) {
                throw new RuntimeException("Uno o más permisos no existen");
            }
            rol.setPermisos(permisos);
        }

        Rol actualizado = rolRepository.save(rol);
        return convertirAResponse(actualizado);
    }

    /**
     * Eliminar rol
     */
    @Transactional
    public void eliminar(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // No permitir eliminar rol Administrador
        if (rol.getNombre().equals("Administrador")) {
            throw new RuntimeException("No se puede eliminar el rol Administrador");
        }

        // Verificar que no tenga usuarios asignados
        if (!rol.getUsuarios().isEmpty()) {
            throw new RuntimeException("No se puede eliminar un rol que tiene usuarios asignados");
        }

        // Limpiar las relaciones antes de eliminar para evitar problemas de cascada
        rol.getRolPermisos().clear();
        rol.getUsuarioRoles().clear();
        rol.getPermisos().clear();
        
        // Guardar los cambios antes de eliminar
        rolRepository.save(rol);
        rolRepository.flush();
        
        // Ahora eliminar el rol
        rolRepository.delete(rol);
    }

    /**
     * Convertir entidad a DTO de respuesta
     */
    private RolResponse convertirAResponse(Rol rol) {
        RolResponse response = new RolResponse();
        response.setId(rol.getId());
        response.setNombre(rol.getNombre());
        response.setDescripcion(rol.getDescripcion());

        // Convertir permisos
        List<PermisoResponse> permisosResponse = rol.getPermisos().stream()
                .map(p -> new PermisoResponse(
                    p.getId(),
                    p.getCodigo(), // Usamos codigo en lugar de nombre
                    p.getDescripcion(),
                    p.getModulo()
                ))
                .collect(Collectors.toList());
        
        response.setPermisos(permisosResponse);
        return response;
    }
}
