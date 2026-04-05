package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.service.ReportesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportesController {

    private final ReportesService reportesService;

    /**
     * GET /api/reportes/ingresos/mensual
     * Reporte de ingresos de un mes específico
     * 
     * @param anio Año (ej: 2025)
     * @param mes Mes (1-12)
     */
    @GetMapping("/ingresos/mensual")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<ReporteIngresosDTO> obtenerReporteIngresosMensual(
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        log.info("📊 GET /api/reportes/ingresos/mensual?anio={}&mes={}", anio, mes);
        
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().build();
        }
        
        ReporteIngresosDTO reporte = reportesService.obtenerReporteIngresosMensual(anio, mes);
        return ResponseEntity.ok(reporte);
    }

    /**
     * GET /api/reportes/ingresos/anual
     * Reporte de ingresos mes a mes de un año completo
     * 
     * @param anio Año (ej: 2025)
     */
    @GetMapping("/ingresos/anual")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteIngresosDTO>> obtenerReporteIngresosAnual(
            @RequestParam int anio
    ) {
        log.info("📅 GET /api/reportes/ingresos/anual?anio={}", anio);
        
        List<ReporteIngresosDTO> reportes = reportesService.obtenerReporteIngresosAnual(anio);
        return ResponseEntity.ok(reportes);
    }

    /**
     * GET /api/reportes/asistencias/por-cliente
     * Reporte de asistencias agrupadas por cliente
     * 
     * @param inicio Fecha de inicio (formato: YYYY-MM-DD)
     * @param fin Fecha de fin (formato: YYYY-MM-DD)
     */
    @GetMapping("/asistencias/por-cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteAsistenciaClienteDTO>> obtenerReporteAsistenciasPorCliente(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("👥 GET /api/reportes/asistencias/por-cliente?inicio={}&fin={}", inicio, fin);
        
        if (fin.isBefore(inicio)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ReporteAsistenciaClienteDTO> reportes = reportesService.obtenerReporteAsistenciasPorCliente(inicio, fin);
        return ResponseEntity.ok(reportes);
    }

    /**
     * GET /api/reportes/membresias/mas-vendidas
     * Reporte de membresías más vendidas y rentables
     * 
     * @param inicio Fecha de inicio (formato: YYYY-MM-DD)
     * @param fin Fecha de fin (formato: YYYY-MM-DD)
     */
    @GetMapping("/membresias/mas-vendidas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReporteMembresiaDTO>> obtenerReporteMembresiasMasVendidas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("🎫 GET /api/reportes/membresias/mas-vendidas?inicio={}&fin={}", inicio, fin);
        
        if (fin.isBefore(inicio)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ReporteMembresiaDTO> reportes = reportesService.obtenerReporteMembresiasMasVendidas(inicio, fin);
        return ResponseEntity.ok(reportes);
    }

    /**
     * GET /api/reportes/comparativo
     * Reporte comparativo mes actual vs mes anterior
     */
    @GetMapping("/comparativo")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteComparativoDTO>> obtenerReporteComparativo() {
        log.info("📈 GET /api/reportes/comparativo");
        
        List<ReporteComparativoDTO> reportes = reportesService.obtenerReporteComparativo();
        return ResponseEntity.ok(reportes);
    }

    /**
     * GET /api/reportes/asistencias/tendencia
     * Tendencia de asistencias día a día
     */
    @GetMapping("/asistencias/tendencia")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteTendenciaDTO>> obtenerTendenciaAsistencias(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("📈 GET /api/reportes/asistencias/tendencia?inicio={}&fin={}", inicio, fin);
        List<ReporteTendenciaDTO> tendencia = reportesService.obtenerTendenciaAsistencias(inicio, fin);
        return ResponseEntity.ok(tendencia);
    }

    /**
     * GET /api/reportes/asistencias/horas-pico
     * Horas del día con más asistencias
     */
    @GetMapping("/asistencias/horas-pico")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteHoraPicoDTO>> obtenerHorasPico(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("🕐 GET /api/reportes/asistencias/horas-pico?inicio={}&fin={}", inicio, fin);
        List<ReporteHoraPicoDTO> horasPico = reportesService.obtenerHorasPico(inicio, fin);
        return ResponseEntity.ok(horasPico);
    }

    /**
     * GET /api/reportes/asistencias/clientes-ausentes
     * Clientes que no han asistido recientemente
     */
    @GetMapping("/asistencias/clientes-ausentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteClienteAusenteDTO>> obtenerClientesAusentes(
            @RequestParam(defaultValue = "7") Integer diasAusencia
    ) {
        log.info("😴 GET /api/reportes/asistencias/clientes-ausentes?diasAusencia={}", diasAusencia);
        List<ReporteClienteAusenteDTO> ausentes = reportesService.obtenerClientesAusentes(diasAusencia);
        return ResponseEntity.ok(ausentes);
    }

    /**
     * GET /api/reportes/asistencias/top-clientes
     * Top clientes por asistencia
     */
    @GetMapping("/asistencias/top-clientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteAsistenciaClienteDTO>> obtenerTopClientes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(defaultValue = "10") Integer limite
    ) {
        log.info("🎯 GET /api/reportes/asistencias/top-clientes?inicio={}&fin={}&limite={}", inicio, fin, limite);
        List<ReporteAsistenciaClienteDTO> topClientes = reportesService.obtenerTopClientesPorAsistencia(inicio, fin, limite);
        return ResponseEntity.ok(topClientes);
    }

    /**
     * GET /api/reportes/asistencias/recientes
     * Últimas asistencias registradas
     */
    @GetMapping("/asistencias/recientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteAsistenciaRecienteDTO>> obtenerAsistenciasRecientes(
            @RequestParam(defaultValue = "10") Integer limite
    ) {
        log.info("⏰ GET /api/reportes/asistencias/recientes?limite={}", limite);
        List<ReporteAsistenciaRecienteDTO> recientes = reportesService.obtenerAsistenciasRecientes(limite);
        return ResponseEntity.ok(recientes);
    }

    /**
     * GET /api/reportes/suscripciones/distribucion-estado
     * Distribución de clientes por estado de suscripción
     */
    @GetMapping("/suscripciones/distribucion-estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteDistribucionDTO>> obtenerDistribucionPorEstado() {
        log.info("📊 GET /api/reportes/suscripciones/distribucion-estado");
        List<ReporteDistribucionDTO> distribucion = reportesService.obtenerDistribucionPorEstado();
        return ResponseEntity.ok(distribucion);
    }

    /**
     * GET /api/reportes/suscripciones/distribucion-membresia
     * Distribución de clientes por tipo de membresía
     */
    @GetMapping("/suscripciones/distribucion-membresia")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteDistribucionDTO>> obtenerDistribucionPorMembresia() {
        log.info("📊 GET /api/reportes/suscripciones/distribucion-membresia");
        List<ReporteDistribucionDTO> distribucion = reportesService.obtenerDistribucionPorMembresia();
        return ResponseEntity.ok(distribucion);
    }

    /**
     * GET /api/reportes/suscripciones/proximos-vencer
     * Clientes cuya membresía está próxima a vencer
     */
    @GetMapping("/suscripciones/proximos-vencer")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteClienteProximoVencerDTO>> obtenerClientesProximosVencer(
            @RequestParam(defaultValue = "15") Integer diasAnticipacion
    ) {
        log.info("⚠️ GET /api/reportes/suscripciones/proximos-vencer?diasAnticipacion={}", diasAnticipacion);
        List<ReporteClienteProximoVencerDTO> proximosVencer = reportesService.obtenerClientesProximosVencer(diasAnticipacion);
        return ResponseEntity.ok(proximosVencer);
    }

    /**
     * GET /api/reportes/ingresos/tendencia
     * Tendencia de ingresos día a día
     */
    @GetMapping("/ingresos/tendencia")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteTendenciaDTO>> obtenerTendenciaIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("💰 GET /api/reportes/ingresos/tendencia?inicio={}&fin={}", inicio, fin);
        List<ReporteTendenciaDTO> tendencia = reportesService.obtenerTendenciaIngresos(inicio, fin);
        return ResponseEntity.ok(tendencia);
    }

    /**
     * GET /api/reportes/ingresos/historial-pagos
     * Historial de pagos recientes
     */
    @GetMapping("/ingresos/historial-pagos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReportePagoHistorialDTO>> obtenerHistorialPagos(
            @RequestParam(defaultValue = "20") Integer limite
    ) {
        log.info("💳 GET /api/reportes/ingresos/historial-pagos?limite={}", limite);
        List<ReportePagoHistorialDTO> historial = reportesService.obtenerHistorialPagos(limite);
        return ResponseEntity.ok(historial);
    }

    /**
     * GET /api/reportes/ingresos/distribucion-plan
     * Distribución de ingresos por tipo de plan
     */
    @GetMapping("/ingresos/distribucion-plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteDistribucionDTO>> obtenerDistribucionIngresosPorPlan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("📊 GET /api/reportes/ingresos/distribucion-plan?inicio={}&fin={}", inicio, fin);
        List<ReporteDistribucionDTO> distribucion = reportesService.obtenerDistribucionIngresosPorPlan(inicio, fin);
        return ResponseEntity.ok(distribucion);
    }

    /**
     * GET /api/reportes/metricas-comparativas
     * Métricas del dashboard con comparación vs periodo anterior
     * Acepta parámetros de rango de fechas para filtrar según Día/Semana/Mes/Año
     */
    @GetMapping("/metricas-comparativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<MetricaComparativaDTO>> obtenerMetricasComparativas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("📊 GET /api/reportes/metricas-comparativas?inicio={}&fin={}", inicio, fin);
        List<MetricaComparativaDTO> metricas = reportesService.obtenerMetricasComparativas(inicio, fin);
        return ResponseEntity.ok(metricas);
    }

    /**
     * GET /api/reportes/ingresos/metodos-pago
     * Distribución de ingresos por método de pago (efectivo, tarjeta, transferencia)
     */
    @GetMapping("/ingresos/metodos-pago")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteMetodoPagoDTO>> obtenerDistribucionMetodosPago(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("💳 GET /api/reportes/ingresos/metodos-pago?inicio={}&fin={}", inicio, fin);
        List<ReporteMetodoPagoDTO> distribucion = reportesService.obtenerDistribucionMetodosPago(inicio, fin);
        return ResponseEntity.ok(distribucion);
    }

    /**
     * GET /api/reportes/suscripciones/renovaciones-cancelaciones
     * Histórico de renovaciones y cancelaciones
     * Acepta parámetros opcionales inicio y fin para filtrar por rango de fechas
     */
    @GetMapping("/suscripciones/renovaciones-cancelaciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteRenovacionCancelacionDTO>> obtenerRenovacionesCancelaciones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        log.info("📊 GET /api/reportes/suscripciones/renovaciones-cancelaciones - inicio: {}, fin: {}", inicio, fin);
        List<ReporteRenovacionCancelacionDTO> reporte = reportesService.obtenerRenovacionesCancelaciones(inicio, fin);
        return ResponseEntity.ok(reporte);
    }
}

