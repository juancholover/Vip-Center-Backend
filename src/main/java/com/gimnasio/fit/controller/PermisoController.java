package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.PermisoResponse;
import com.gimnasio.fit.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestión de permisos
 */
@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    /**
     * Listar todos los permisos disponibles
     */
    @GetMapping
    public ResponseEntity<List<PermisoResponse>> listar(){
        List<PermisoResponse> permisos = permisoService.obtenerTodos();
        return ResponseEntity.ok(permisos);
    }
}