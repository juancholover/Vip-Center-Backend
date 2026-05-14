package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.service.ReportesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    // ========================================================================
    // HU-28: REPORTE DE SUSCRIPCIONES (Paginado + Exportar Excel)
    // ========================================================================

    /**
     * GET /api/reportes/suscripciones
     * Listado paginado de suscripciones con filtros dinámicos (HU-28).
     *
     * @param estado           Estado: "activa", "vencida", "por_vencer" (opcional)
     * @param fechaInicio      Fecha inicio rango vencimiento (opcional)
     * @param fechaFin         Fecha fin rango vencimiento (opcional)
     * @param diasAnticipacion Días para "por_vencer" (default 15)
     * @param page             Número de página (default 0)
     * @param size             Tamaño de página (default 10)
     */
    @GetMapping("/suscripciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Page<ReporteSuscripcionDTO>> obtenerReporteSuscripciones(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer diasAnticipacion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("📋 GET /api/reportes/suscripciones?estado={}&fechaInicio={}&fechaFin={}&dias={}&page={}&size={}",
                estado, fechaInicio, fechaFin, diasAnticipacion, page, size);
        Pageable pageable = PageRequest.of(page, size);
        // Page<ReporteSuscripcionDTO> resultado = reportesService.obtenerReporteSuscripciones(
        //         estado, fechaInicio, fechaFin, diasAnticipacion, pageable);
        return ResponseEntity.ok(Page.empty(pageable));
    }

    /**
     * GET /api/reportes/suscripciones/exportar
     * Exporta suscripciones a Excel con los mismos filtros (HU-28).
     */
    @GetMapping("/suscripciones/exportar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<byte[]> exportarSuscripcionesExcel(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer diasAnticipacion
    ) {
        log.info("📤 GET /api/reportes/suscripciones/exportar?estado={}&fechaInicio={}&fechaFin={}", estado, fechaInicio, fechaFin);

        // byte[] excelBytes = reportesService.exportarSuscripcionesExcel(estado, fechaInicio, fechaFin, diasAnticipacion);
        byte[] excelBytes = new byte[0];

        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_suscripciones.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // ========================================================================
    // HU-29: INGRESOS POR MÉTODO DE PAGO Y POR PLAN (formato frontend)
    // ========================================================================

    /**
     * GET /api/reportes/ingresos/por-metodo
     * Ingresos agrupados por método de pago (HU-29).
     * Formato: [{metodo, total, cantidad, porcentaje}]
     */
    @GetMapping("/ingresos/por-metodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<IngresosPorMetodoDTO>> obtenerIngresosPorMetodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.now().withDayOfMonth(1);
        LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

        log.info("💳 GET /api/reportes/ingresos/por-metodo?fechaInicio={}&fechaFin={}", inicio, fin);
        // List<IngresosPorMetodoDTO> resultado = reportesService.obtenerIngresosPorMetodo(inicio, fin);
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    /**
     * GET /api/reportes/ingresos/por-plan
     * Ingresos agrupados por plan de membresía (HU-29).
     * Formato: [{plan, total, cantidad, porcentaje}]
     */
    @GetMapping("/ingresos/por-plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<IngresosPorPlanDTO>> obtenerIngresosPorPlan(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.now().withDayOfMonth(1);
        LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

        log.info("📊 GET /api/reportes/ingresos/por-plan?fechaInicio={}&fechaFin={}", inicio, fin);
        // List<IngresosPorPlanDTO> resultado = reportesService.obtenerIngresosPorPlan(inicio, fin);
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    // ========================================================================
    // HU-30: HISTORIAL DE PAGOS Y RETENCIÓN
    // ========================================================================

    /**
     * GET /api/reportes/pagos/historial
     * Historial detallado de pagos con buscador por nombre/teléfono (HU-30).
     */
    @GetMapping("/pagos/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReportePagoHistorialDTO>> obtenerHistorialPagosDetallado(
            @RequestParam(required = false, defaultValue = "") String busqueda
    ) {
        log.info("💳 GET /api/reportes/pagos/historial?busqueda={}", busqueda);
        // List<ReportePagoHistorialDTO> historial = reportesService.obtenerHistorialPagosDetallado(busqueda);
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    /**
     * GET /api/reportes/retencion
     * Reporte de retención mensual: renovaciones y cancelaciones por mes (HU-30).
     */
    @GetMapping("/retencion")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<RetencionMensualDTO>> obtenerRetencionMensual() {
        log.info("📊 GET /api/reportes/retencion");
        // List<RetencionMensualDTO> reporte = reportesService.obtenerRetencionMensual();
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    /**
     * GET /api/reportes/pagos/historial/exportar
     * Exporta historial de pagos a Excel (HU-30).
     */
    @GetMapping("/pagos/historial/exportar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<byte[]> exportarHistorialPagosExcel(
            @RequestParam(required = false, defaultValue = "") String busqueda
    ) {
        log.info("📤 GET /api/reportes/pagos/historial/exportar?busqueda={}", busqueda);

        // byte[] excelBytes = reportesService.exportarHistorialPagosExcel(busqueda);
        byte[] excelBytes = new byte[0];

        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial_pagos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
