package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.PermisoResponse;
import com.gimnasio.fit.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de permisos (EPIC 2.5)
 */
@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;

    /**
     * Listar todos los permisos disponibles
     */
    public List<PermisoResponse> obtenerTodos() {
        return permisoRepository.findAll().stream()
                .map(p -> new PermisoResponse(
                    p.getId(),
                    p.getCodigo(), // Usamos codigo en lugar de nombre
                    p.getDescripcion(),
                    p.getModulo()
                ))
                .collect(Collectors.toList());
    }
}
