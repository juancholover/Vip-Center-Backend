package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.InactividadResumenDTO;
import com.gimnasio.fit.service.ClienteInactividadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para detección de clientes inactivos (HU-34) y exportación (HU-35).
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClienteInactividadController {

    private final ClienteInactividadService inactividadService;

    /**
     * GET /api/clientes/inactividad
     * Panel de alertas de inactividad (HU-34).
     * Devuelve clientes agrupados por nivel de riesgo con conteos.
     *
     * @param diasMinimo Mínimo de días de inactividad para incluir (default 0 = todos)
     *                   Usar 15 para mostrar solo medio/alto/critico.
     * @param nivelRiesgo Filtro opcional por nivel: "BAJO", "MEDIO", "ALTO", "CRITICO"
     */
    @GetMapping("/inactividad")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<InactividadResumenDTO> obtenerClientesInactivos(
            @RequestParam(defaultValue = "0") Integer diasMinimo,
            @RequestParam(required = false) String nivelRiesgo
    ) {
        log.info("😴 GET /api/clientes/inactividad?diasMinimo={}&nivelRiesgo={}", diasMinimo, nivelRiesgo);

        InactividadResumenDTO resumen = inactividadService.obtenerClientesInactivos(diasMinimo);

        // Filtrar por nivel de riesgo si se especifica
        if (nivelRiesgo != null && !nivelRiesgo.isBlank()) {
            String filtro = nivelRiesgo.toUpperCase();
            resumen.getClientes().removeIf(c -> !filtro.equals(c.getNivelRiesgo()));
            resumen.setTotalInactivos(resumen.getClientes().size());
        }

        return ResponseEntity.ok(resumen);
    }

    /**
     * GET /api/clientes/exportar-inactivos
     * Exporta lista de clientes inactivos a Excel (HU-35).
     * Solo accesible para ADMIN (datos sensibles: teléfono, email).
     *
     * @param diasMinimo Mínimo de días de inactividad (default 15)
     */
    @GetMapping("/exportar-inactivos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarClientesInactivos(
            @RequestParam(defaultValue = "15") Integer diasMinimo
    ) {
        log.info("📤 GET /api/clientes/exportar-inactivos?diasMinimo={}", diasMinimo);

        byte[] excelBytes = inactividadService.exportarClientesInactivosExcel(diasMinimo);

        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clientes_inactivos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
