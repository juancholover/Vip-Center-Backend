package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para endpoints del dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/stats
     * Obtiene las estadísticas principales del dashboard.
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardStatsDTO> obtenerEstadisticas() {
        log.info("📊 GET /api/dashboard/stats");
        DashboardStatsDTO stats = dashboardService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/dashboard/ingresos-semana
     * Obtiene los ingresos de los últimos 7 días.
     */
    @GetMapping("/ingresos-semana")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IngresosDiaDTO>> obtenerIngresosSemana() {
        log.info("📈 GET /api/dashboard/ingresos-semana");
        List<IngresosDiaDTO> ingresos = dashboardService.obtenerIngresosSemana();
        return ResponseEntity.ok(ingresos);
    }

    /**
     * GET /api/dashboard/asistencias-por-hora
     * Obtiene las asistencias agrupadas por hora del día actual.
     */
    @GetMapping("/asistencias-por-hora")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AsistenciasPorHoraDTO>> obtenerAsistenciasPorHora() {
        log.info("📊 GET /api/dashboard/asistencias-por-hora");
        List<AsistenciasPorHoraDTO> asistencias = dashboardService.obtenerAsistenciasPorHora();
        return ResponseEntity.ok(asistencias);
    }

    /**
     * GET /api/dashboard/actividad-reciente
     * Obtiene las últimas 10 actividades recientes.
     */
    @GetMapping("/actividad-reciente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActividadRecienteDTO>> obtenerActividadReciente() {
        log.info("🔔 GET /api/dashboard/actividad-reciente");
        List<ActividadRecienteDTO> actividades = dashboardService.obtenerActividadReciente();
        return ResponseEntity.ok(actividades);
    }

    /**
     * GET /api/dashboard/asistencias-tendencia
     * Obtiene la tendencia de asistencias agrupadas por día (HU-26).
     * 
     * @param dias Cantidad de días hacia atrás (default: 7, opciones: 7 o 30)
     */
    @GetMapping("/asistencias-tendencia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AsistenciaTendenciaDTO>> obtenerTendenciaAsistencias(
            @RequestParam(defaultValue = "7") Integer dias
    ) {
        log.info("📈 GET /api/dashboard/asistencias-tendencia?dias={}", dias);
        List<AsistenciaTendenciaDTO> tendencia = dashboardService.obtenerTendenciaAsistencias(dias);
        return ResponseEntity.ok(tendencia);
    }

    /**
     * GET /api/dashboard/top-clientes
     * Obtiene el ranking de clientes con más asistencias del mes actual (HU-26).
     * 
     * @param limite Cantidad máxima de clientes (default: 10)
     */
    @GetMapping("/top-clientes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TopClienteDTO>> obtenerTopClientes(
            @RequestParam(defaultValue = "10") Integer limite
    ) {
        log.info("🏆 GET /api/dashboard/top-clientes?limite={}", limite);
        List<TopClienteDTO> topClientes = dashboardService.obtenerTopClientes(limite);
        return ResponseEntity.ok(topClientes);
    }
}
