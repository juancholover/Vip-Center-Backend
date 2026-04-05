package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.DescuentoDTO;
import com.gimnasio.fit.service.DescuentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/descuentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class DescuentoController {

    private final DescuentoService descuentoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<DescuentoDTO>> listarTodos() {
        return ResponseEntity.ok(descuentoService.listarTodos());
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<DescuentoDTO>> listarActivos() {
        return ResponseEntity.ok(descuentoService.listarActivos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<DescuentoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(descuentoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentoDTO> crear(@RequestBody DescuentoDTO dto) {
        return ResponseEntity.ok(descuentoService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentoDTO> actualizar(@PathVariable Long id, @RequestBody DescuentoDTO dto) {
        return ResponseEntity.ok(descuentoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DescuentoDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload
    ) {
        Boolean nuevoEstado = payload.get("estado");
        return ResponseEntity.ok(descuentoService.cambiarEstado(id, nuevoEstado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        descuentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recalcular-orden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> recalcularOrden() {
        descuentoService.recalcularOrden();
        return ResponseEntity.ok().build();
    }
}
