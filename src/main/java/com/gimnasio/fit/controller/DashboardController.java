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
}
