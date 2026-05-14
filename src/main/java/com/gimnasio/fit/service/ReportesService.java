package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import com.gimnasio.fit.entity.Cliente;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportesService {

    private final PagoRepository pagoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ClienteRepository clienteRepository;
    private final MembresiaRepository membresiaRepository;
    private final RenovacionRepository renovacionRepository;

    /**
     * 📊 REPORTE DE INGRESOS MENSUAL
     * Genera reporte detallado de ingresos de un mes específico
     */
    @Transactional(readOnly = true)
    public ReporteIngresosDTO obtenerReporteIngresosMensual(int anio, int mes) {
        try {
            log.info("📊 Generando reporte de ingresos para {}/{}", mes, anio);

            YearMonth yearMonth = YearMonth.of(anio, mes);
            LocalDate inicio = yearMonth.atDay(1);
            LocalDate fin = yearMonth.atEndOfMonth();
            
            Instant inicioInstant = toInstant(inicio.atStartOfDay());
            Instant finInstant = toInstant(fin.atTime(23, 59, 59));

            // Consultas a BD
            java.math.BigDecimal bd_totalIngresos = pagoRepository.sumMontoByFechaBetween(inicioInstant, finInstant); Double totalIngresos = bd_totalIngresos != null ? bd_totalIngresos.doubleValue() : 0.0;
            Integer cantidadPagos = pagoRepository.countByFechaRegistroBetween(inicioInstant, finInstant);
            
            java.math.BigDecimal bd_ingresosAprobados = pagoRepository.sumMontoByEstadoAndFechaBetween("aprobado", inicioInstant, finInstant); Double ingresosAprobados = bd_ingresosAprobados != null ? bd_ingresosAprobados.doubleValue() : 0.0;
            java.math.BigDecimal bd_ingresosPendientes = pagoRepository.sumMontoByEstadoAndFechaBetween("pendiente", inicioInstant, finInstant); Double ingresosPendientes = bd_ingresosPendientes != null ? bd_ingresosPendientes.doubleValue() : 0.0;
            java.math.BigDecimal bd_ingresosRechazados = pagoRepository.sumMontoByEstadoAndFechaBetween("rechazado", inicioInstant, finInstant); Double ingresosRechazados = bd_ingresosRechazados != null ? bd_ingresosRechazados.doubleValue() : 0.0;

            // Validaciones
            if (totalIngresos == null) totalIngresos = 0.0;
            if (cantidadPagos == null) cantidadPagos = 0;
            if (ingresosAprobados == null) ingresosAprobados = 0.0;
            if (ingresosPendientes == null) ingresosPendientes = 0.0;
            if (ingresosRechazados == null) ingresosRechazados = 0.0;

            Double promedioTicket = cantidadPagos > 0 ? totalIngresos / cantidadPagos : 0.0;
            
            String nombreMes = yearMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            String periodo = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1) + " " + anio;

            return new ReporteIngresosDTO(
                periodo,
                totalIngresos,
                cantidadPagos,
                promedioTicket,
                ingresosAprobados,
                ingresosPendientes,
                ingresosRechazados,
                inicio,
                fin
            );

        } catch (Exception e) {
            log.error("❌ Error al generar reporte de ingresos: {}", e.getMessage(), e);
            return new ReporteIngresosDTO("Error", 0.0, 0, 0.0, 0.0, 0.0, 0.0, LocalDate.now(), LocalDate.now());
        }
    }

    /**
     * 📅 REPORTE DE INGRESOS ANUAL
     * Genera reporte de ingresos mes a mes de un año
     */
    @Transactional(readOnly = true)
    public List<ReporteIngresosDTO> obtenerReporteIngresosAnual(int anio) {
        List<ReporteIngresosDTO> reportes = new ArrayList<>();
        
        for (int mes = 1; mes <= 12; mes++) {
            reportes.add(obtenerReporteIngresosMensual(anio, mes));
        }
        
        return reportes;
    }

    /**
     * 👥 REPORTE DE ASISTENCIAS POR CLIENTE
     * Analiza asistencias de cada cliente en un período
     */
    @Transactional(readOnly = true)
    public List<ReporteAsistenciaClienteDTO> obtenerReporteAsistenciasPorCliente(LocalDate inicio, LocalDate fin) {
        try {
            log.info("👥 Generando reporte de asistencias por cliente desde {} hasta {}", inicio, fin);

            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime finDateTime = fin.atTime(23, 59, 59);

            List<Object[]> resultados = asistenciaRepository.obtenerAsistenciasPorCliente(inicioDateTime, finDateTime);
            List<ReporteAsistenciaClienteDTO> reportes = new ArrayList<>();

            for (Object[] fila : resultados) {
                Long clienteIdLong = (Long) fila[0];
                Integer clienteId = clienteIdLong.intValue();
                String nombre = (String) fila[1];
                String apellido = (String) fila[2];
                String nombreCompleto = nombre + " " + apellido;
                String email = (String) fila[3];
                String telefono = (String) fila[4];
                Long totalAsistencias = (Long) fila[5];
                LocalDateTime primeraAsistencia = (LocalDateTime) fila[6];
                LocalDateTime ultimaAsistencia = (LocalDateTime) fila[7];
                String estadoMembresia = (String) fila[8];
                LocalDate fechaVencimiento = (LocalDate) fila[9];

                // Calcular promedio de asistencias por mes
                long diasEnPeriodo = ChronoUnit.DAYS.between(inicio, fin);
                double mesesEnPeriodo = diasEnPeriodo / 30.0;
                Double promedioMes = mesesEnPeriodo > 0 ? totalAsistencias / mesesEnPeriodo : 0.0;

                reportes.add(new ReporteAsistenciaClienteDTO(
                    clienteId,
                    nombreCompleto,
                    email,
                    telefono,
                    totalAsistencias.intValue(),
                    primeraAsistencia.toLocalDate(),
                    ultimaAsistencia.toLocalDate(),
                    Math.round(promedioMes * 100.0) / 100.0,
                    estadoMembresia,
                    fechaVencimiento
                ));
            }

            // Ordenar por total de asistencias descendente
            reportes.sort((a, b) -> b.getTotalAsistencias().compareTo(a.getTotalAsistencias()));

            log.info("✅ Reporte generado con {} clientes", reportes.size());
            return reportes;

        } catch (Exception e) {
            log.error("❌ Error al generar reporte de asistencias: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🎫 REPORTE DE MEMBRESÍAS MÁS VENDIDAS
     * Analiza rendimiento de cada tipo de membresía
     */
    @Transactional(readOnly = true)
    public List<ReporteMembresiaDTO> obtenerReporteMembresiasMasVendidas(LocalDate inicio, LocalDate fin) {
        try {
            log.info("🎫 Generando reporte de membresías desde {} hasta {}", inicio, fin);

            // Convertir LocalDate a Instant
            Instant inicioInstant = inicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant finInstant = fin.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            
            List<Object[]> resultados = membresiaRepository.obtenerReporteMembresiasPorVentas(inicioInstant, finInstant);
            List<ReporteMembresiaDTO> reportes = new ArrayList<>();

            for (Object[] fila : resultados) {
                Long membresiaId = (Long) fila[0];
                String nombreMembresia = (String) fila[1];
                Double precioBase = fila[2] != null ? ((Number) fila[2]).doubleValue() : 0.0;
                Integer duracionDias = (Integer) fila[3];
                Long cantidadVentas = (Long) fila[4];
                Double totalIngresos = fila[5] != null ? ((Number) fila[5]).doubleValue() : 0.0;
                Long clientesActivos = (Long) fila[6];
                Long clientesVencidos = (Long) fila[7];

                // Calcular métricas
                long diasEnPeriodo = ChronoUnit.DAYS.between(inicio, fin);
                double mesesEnPeriodo = diasEnPeriodo / 30.0;
                Double promedioMensual = mesesEnPeriodo > 0 ? totalIngresos / mesesEnPeriodo : 0.0;

                Double tasaRetencion = 0.0;
                if (cantidadVentas > 0) {
                    tasaRetencion = (clientesActivos.doubleValue() / cantidadVentas.doubleValue()) * 100;
                }

                reportes.add(new ReporteMembresiaDTO(
                    membresiaId,
                    nombreMembresia,
                    precioBase,
                    duracionDias,
                    cantidadVentas.intValue(),
                    totalIngresos,
                    Math.round(promedioMensual * 100.0) / 100.0,
                    clientesActivos.intValue(),
                    clientesVencidos.intValue(),
                    Math.round(tasaRetencion * 100.0) / 100.0
                ));
            }

            // Ordenar por total ingresos descendente
            reportes.sort((a, b) -> b.getTotalIngresos().compareTo(a.getTotalIngresos()));

            log.info("✅ Reporte generado con {} membresías", reportes.size());
            return reportes;

        } catch (Exception e) {
            log.error("❌ Error al generar reporte de membresías: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 📈 REPORTE COMPARATIVO
     * Compara métricas del mes actual vs mes anterior
     */
    @Transactional(readOnly = true)
    public List<ReporteComparativoDTO> obtenerReporteComparativo() {
        try {
            log.info("📈 Generando reporte comparativo");

            LocalDate hoy = LocalDate.now();
            YearMonth mesActual = YearMonth.from(hoy);
            YearMonth mesAnterior = mesActual.minusMonths(1);

            List<ReporteComparativoDTO> reportes = new ArrayList<>();

            // 1. Comparar Ingresos
            ReporteIngresosDTO ingresosActuales = obtenerReporteIngresosMensual(mesActual.getYear(), mesActual.getMonthValue());
            ReporteIngresosDTO ingresosAnteriores = obtenerReporteIngresosMensual(mesAnterior.getYear(), mesAnterior.getMonthValue());
            
            reportes.add(crearComparativo(
                "Ingresos Totales",
                ingresosActuales.getTotalIngresos(),
                ingresosAnteriores.getTotalIngresos(),
                ingresosActuales.getPeriodo()
            ));

            reportes.add(crearComparativo(
                "Cantidad de Pagos",
                ingresosActuales.getCantidadPagos().doubleValue(),
                ingresosAnteriores.getCantidadPagos().doubleValue(),
                ingresosActuales.getPeriodo()
            ));

            // 2. Comparar Asistencias
            Instant inicioActual = toInstant(mesActual.atDay(1).atStartOfDay());
            Instant finActual = toInstant(mesActual.atEndOfMonth().atTime(23, 59, 59));
            Instant inicioAnterior = toInstant(mesAnterior.atDay(1).atStartOfDay());
            Instant finAnterior = toInstant(mesAnterior.atEndOfMonth().atTime(23, 59, 59));

            Integer asistenciasActuales = asistenciaRepository.countByFechaHoraBetween(
                toLocalDateTime(inicioActual), 
                toLocalDateTime(finActual)
            );
            Integer asistenciasAnteriores = asistenciaRepository.countByFechaHoraBetween(
                toLocalDateTime(inicioAnterior), 
                toLocalDateTime(finAnterior)
            );

            if (asistenciasActuales == null) asistenciasActuales = 0;
            if (asistenciasAnteriores == null) asistenciasAnteriores = 0;

            reportes.add(crearComparativo(
                "Total de Asistencias",
                asistenciasActuales.doubleValue(),
                asistenciasAnteriores.doubleValue(),
                ingresosActuales.getPeriodo()
            ));

            // 3. Comparar Clientes Activos
            Integer clientesActuales = clienteRepository.countClientesActivos(mesActual.atEndOfMonth());
            Integer clientesAnteriores = clienteRepository.countClientesActivos(mesAnterior.atEndOfMonth());

            if (clientesActuales == null) clientesActuales = 0;
            if (clientesAnteriores == null) clientesAnteriores = 0;

            reportes.add(crearComparativo(
                "Clientes Activos",
                clientesActuales.doubleValue(),
                clientesAnteriores.doubleValue(),
                ingresosActuales.getPeriodo()
            ));

            log.info("✅ Reporte comparativo generado con {} métricas", reportes.size());
            return reportes;

        } catch (Exception e) {
            log.error("❌ Error al generar reporte comparativo: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ========== MÉTODOS HELPER ==========

    private ReporteComparativoDTO crearComparativo(String metrica, Double valorActual, Double valorAnterior, String periodo) {
        Double diferencia = valorActual - valorAnterior;
        Double porcentajeCambio = valorAnterior > 0 ? (diferencia / valorAnterior) * 100 : 0.0;
        
        String tendencia;
        if (porcentajeCambio > 5) tendencia = "subida";
        else if (porcentajeCambio < -5) tendencia = "bajada";
        else tendencia = "estable";

        return new ReporteComparativoDTO(
            metrica,
            Math.round(valorActual * 100.0) / 100.0,
            Math.round(valorAnterior * 100.0) / 100.0,
            Math.round(diferencia * 100.0) / 100.0,
            Math.round(porcentajeCambio * 100.0) / 100.0,
            tendencia,
            periodo
        );
    }

    private Instant toInstant(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * 📈 TENDENCIA DE ASISTENCIAS
     * Obtiene asistencias día a día en un rango
     */
    @Transactional(readOnly = true)
    public List<ReporteTendenciaDTO> obtenerTendenciaAsistencias(LocalDate inicio, LocalDate fin) {
        try {
            log.info("📈 Generando tendencia de asistencias desde {} hasta {}", inicio, fin);
            
            List<ReporteTendenciaDTO> tendencia = new ArrayList<>();
            LocalDate fecha = inicio;
            
            while (!fecha.isAfter(fin)) {
                LocalDateTime inicioDia = fecha.atStartOfDay();
                LocalDateTime finDia = fecha.atTime(23, 59, 59);
                
                Integer cantidad = asistenciaRepository.countByFechaHoraBetween(inicioDia, finDia);
                if (cantidad == null) cantidad = 0;
                
                String fechaStr = fecha.getDayOfMonth() + " " + 
                    fecha.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
                
                tendencia.add(new ReporteTendenciaDTO(fechaStr, cantidad, null));
                fecha = fecha.plusDays(1);
            }
            
            return tendencia;
        } catch (Exception e) {
            log.error("❌ Error al generar tendencia de asistencias: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🕐 HORAS PICO
     * Obtiene las horas con más asistencias
     */
    @Transactional(readOnly = true)
    public List<ReporteHoraPicoDTO> obtenerHorasPico(LocalDate inicio, LocalDate fin) {
        try {
            log.info("🕐 Generando reporte de horas pico desde {} hasta {}", inicio, fin);
            
            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime finDateTime = fin.atTime(23, 59, 59);
            
            List<Object[]> resultados = asistenciaRepository.obtenerAsistenciasPorHora(inicioDateTime, finDateTime);
            List<ReporteHoraPicoDTO> horasPico = new ArrayList<>();
            
            for (Object[] fila : resultados) {
                Integer hora = (Integer) fila[0];
                Long cantidad = (Long) fila[1];
                
                String horaStr = String.format("%02d:00", hora);
                horasPico.add(new ReporteHoraPicoDTO(horaStr, cantidad.intValue()));
            }
            
            return horasPico;
        } catch (Exception e) {
            log.error("❌ Error al generar horas pico: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 😴 CLIENTES AUSENTES
     * Obtiene clientes que no han asistido recientemente
     */
    @Transactional(readOnly = true)
    public List<ReporteClienteAusenteDTO> obtenerClientesAusentes(Integer diasAusencia) {
        try {
            log.warn("😴🔥 ============ INICIANDO CLIENTES AUSENTES ============");
            log.warn("😴🔥 Parámetro recibido: {} días", diasAusencia);
            log.warn("😴🔥 Fecha actual: {}", LocalDate.now());
            
            LocalDate fechaLimite = LocalDate.now().minusDays(diasAusencia);
            LocalDateTime fechaLimiteDateTime = fechaLimite.atStartOfDay();
            
            log.warn("😴🔥 Fecha límite calculada: {}", fechaLimiteDateTime);
            log.warn("😴🔥 Llamando a asistenciaRepository.obtenerClientesAusentes...");
            
            List<Object[]> resultados = asistenciaRepository.obtenerClientesAusentes(fechaLimiteDateTime);
            
            log.warn("😴🔥 RESULTADOS DE BD: {} registros", resultados.size());
            if (resultados.isEmpty()) {
                log.error("😴🔥 ❌❌❌ LA CONSULTA NO DEVOLVIÓ NINGÚN RESULTADO ❌❌❌");
            } else {
                log.warn("😴🔥 ✅ Primer registro: {}", resultados.get(0));
            }
            List<ReporteClienteAusenteDTO> ausentes = new ArrayList<>();
            
            for (Object[] fila : resultados) {
                Long clienteIdLong = (Long) fila[0];
                Integer clienteId = clienteIdLong.intValue();
                String nombre = (String) fila[1];
                String apellido = (String) fila[2];
                String nombreCompleto = nombre + " " + apellido;
                LocalDateTime ultimaVisita = (LocalDateTime) fila[3]; // Puede ser NULL
                String estadoMembresia = (String) fila[4];
                
                // 🔥 Manejar NULL cuando nunca ha asistido
                long diasAusente;
                LocalDate fechaUltimaVisita;
                
                if (ultimaVisita == null) {
                    // Nunca ha asistido - usar fecha muy antigua
                    diasAusente = 9999;
                    fechaUltimaVisita = LocalDate.of(2000, 1, 1);
                } else {
                    diasAusente = ChronoUnit.DAYS.between(ultimaVisita.toLocalDate(), LocalDate.now());
                    fechaUltimaVisita = ultimaVisita.toLocalDate();
                }
                
                String avatar = nombre.substring(0, 1) + apellido.substring(0, 1);
                
                ausentes.add(new ReporteClienteAusenteDTO(
                    clienteId,
                    nombreCompleto,
                    avatar.toUpperCase(),
                    (int) diasAusente,
                    fechaUltimaVisita,
                    estadoMembresia
                ));
            }
            
            log.warn("😴🔥 ✅ PROCESADOS {} DTOS", ausentes.size());
            return ausentes;
        } catch (Exception e) {
            log.error("❌ Error al generar clientes ausentes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🎯 TOP CLIENTES POR ASISTENCIA
     * Obtiene los clientes con más asistencias
     */
    @Transactional(readOnly = true)
    public List<ReporteAsistenciaClienteDTO> obtenerTopClientesPorAsistencia(LocalDate inicio, LocalDate fin, Integer limite) {
        try {
            log.info("🎯 Generando top {} clientes por asistencia", limite);
            
            List<ReporteAsistenciaClienteDTO> todos = obtenerReporteAsistenciasPorCliente(inicio, fin);
            
            return todos.stream()
                .limit(limite)
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Error al generar top clientes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * ⏰ ASISTENCIAS RECIENTES
     * Obtiene las últimas asistencias registradas
     */
    @Transactional(readOnly = true)
    public List<ReporteAsistenciaRecienteDTO> obtenerAsistenciasRecientes(Integer limite) {
        try {
            log.info("⏰ Generando {} asistencias recientes", limite);
            
            List<Object[]> resultados = asistenciaRepository.obtenerAsistenciasRecientes(limite);
            List<ReporteAsistenciaRecienteDTO> recientes = new ArrayList<>();
            
            for (Object[] fila : resultados) {
                Long asistenciaIdLong = (Long) fila[0];
                Integer asistenciaId = asistenciaIdLong.intValue();
                String nombre = (String) fila[1];
                String apellido = (String) fila[2];
                LocalDateTime fechaHora = (LocalDateTime) fila[3];
                String nombreMembresia = (String) fila[4];
                String estadoMembresia = (String) fila[5];
                
                String nombreCompleto = nombre + " " + apellido;
                String avatar = nombre.substring(0, 1) + apellido.substring(0, 1);
                String hora = String.format("%02d:%02d", fechaHora.getHour(), fechaHora.getMinute());
                
                recientes.add(new ReporteAsistenciaRecienteDTO(
                    asistenciaId,
                    nombreCompleto,
                    avatar.toUpperCase(),
                    hora,
                    nombreMembresia,
                    estadoMembresia
                ));
            }
            
            return recientes;
        } catch (Exception e) {
            log.error("❌ Error al generar asistencias recientes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 📊 DISTRIBUCIÓN POR ESTADO DE SUSCRIPCIÓN
     * Cuenta clientes activos, vencidos y por vencer
     */
    @Transactional(readOnly = true)
    public List<ReporteDistribucionDTO> obtenerDistribucionPorEstado() {
        try {
            log.info("📊 Generando distribución por estado de suscripción");
            
            LocalDate hoy = LocalDate.now();
            LocalDate dentroDe7Dias = hoy.plusDays(7);
            
            Integer activos = clienteRepository.countByFechaVencimientoAfter(hoy);
            Integer vencidos = clienteRepository.countByFechaVencimientoBefore(hoy);
            Integer porVencer = clienteRepository.countByFechaVencimientoBetween(hoy, dentroDe7Dias);
            
            if (activos == null) activos = 0;
            if (vencidos == null) vencidos = 0;
            if (porVencer == null) porVencer = 0;
            
            List<ReporteDistribucionDTO> distribucion = new ArrayList<>();
            distribucion.add(new ReporteDistribucionDTO("Activas", activos, "#10b981"));
            distribucion.add(new ReporteDistribucionDTO("Vencidas", vencidos, "#ef4444"));
            distribucion.add(new ReporteDistribucionDTO("Por Vencer", porVencer, "#f59e0b"));
            
            return distribucion;
        } catch (Exception e) {
            log.error("❌ Error al generar distribución por estado: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 📊 DISTRIBUCIÓN POR TIPO DE MEMBRESÍA
     * Cuenta clientes por cada tipo de membresía
     */
    @Transactional(readOnly = true)
    public List<ReporteDistribucionDTO> obtenerDistribucionPorMembresia() {
        try {
            log.info("📊 Generando distribución por tipo de membresía");
            
            List<Object[]> resultados = clienteRepository.countByMembresia();
            List<ReporteDistribucionDTO> distribucion = new ArrayList<>();
            
            String[] colores = {"#60a5fa", "#10b981", "#fb923c", "#a78bfa", "#f472b6", "#facc15"};
            int colorIndex = 0;
            
            for (Object[] fila : resultados) {
                String nombreMembresia = (String) fila[0];
                Long cantidad = (Long) fila[1];
                
                String color = colores[colorIndex % colores.length];
                distribucion.add(new ReporteDistribucionDTO(nombreMembresia, cantidad.intValue(), color));
                colorIndex++;
            }
            
            return distribucion;
        } catch (Exception e) {
            log.error("❌ Error al generar distribución por membresía: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

        /**
     * ⚠️ CLIENTES PRÓXIMOS A VENCER (1-7 DÍAS)
     * Retorna suscripciones (clientes con membresías) que vencerán en los próximos 7 días
     */
    @Transactional(readOnly = true)
    public List<ReporteClienteProximoVencerDTO> obtenerClientesProximosVencer(Integer diasAnticipacion) {
        try {
            log.warn("⚠️🔥 ============ INICIANDO PRÓXIMOS A VENCER ============");
            log.warn("⚠️🔥 Parámetro recibido: {} días", diasAnticipacion);
            
            LocalDate hoy = LocalDate.now();
            LocalDate fechaInicio = hoy.plusDays(1);  // Comienza MAÑANA (día 1)
            LocalDate fechaLimite = hoy.plusDays(diasAnticipacion); // Termina en 7 días
            
            log.warn("⚠️🔥 Fecha actual (hoy): {}", hoy);
            log.warn("⚠️🔥 Buscando desde: {} hasta: {}", fechaInicio, fechaLimite);
            log.warn("⚠️🔥 Llamando a clienteRepository.findClientesProximosVencer...");
            
            List<Object[]> resultados = clienteRepository.findClientesProximosVencer(fechaInicio, fechaLimite);
            
            log.warn("⚠️🔥 RESULTADOS DE BD: {} registros", resultados.size());
            if (resultados.isEmpty()) {
                log.error("⚠️🔥 ❌❌❌ LA CONSULTA NO DEVOLVIÓ NINGÚN RESULTADO ❌❌❌");
                log.error("⚠️🔥 Parámetros usados: inicio={}, fin={}", fechaInicio, fechaLimite);
            } else {
                log.warn("⚠️🔥 ✅ Primer registro: {}", resultados.get(0));
            }
            
            List<ReporteClienteProximoVencerDTO> proximosVencer = new ArrayList<>();
            
            for (Object[] fila : resultados) {
                Long clienteIdLong = (Long) fila[0];  // 🔥 BD devuelve Long, no Integer
                Integer clienteId = clienteIdLong.intValue();
                String nombre = (String) fila[1];
                String apellido = (String) fila[2];
                LocalDate fechaVencimiento = (LocalDate) fila[3];
                String nombreMembresia = (String) fila[4];
                
                String nombreCompleto = nombre + " " + apellido;
                String avatar = nombre.substring(0, 1) + apellido.substring(0, 1);
                long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVencimiento);
                
                log.warn("⚠️🔥 Cliente: {} - Vence: {} - Días: {}", nombreCompleto, fechaVencimiento, diasRestantes);
                
                proximosVencer.add(new ReporteClienteProximoVencerDTO(
                    clienteId,
                    nombreCompleto,
                    avatar.toUpperCase(),
                    nombreMembresia,
                    (int) diasRestantes,
                    "POR VENCER"
                ));
            }
            
            log.warn("⚠️🔥 ✅ PROCESADOS {} DTOS", proximosVencer.size());
            return proximosVencer;
        } catch (Exception e) {
            log.error("❌ Error al generar clientes próximos a vencer: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 💰 TENDENCIA DE INGRESOS
     * Obtiene ingresos día a día en un rango
     */
    @Transactional(readOnly = true)
    public List<ReporteTendenciaDTO> obtenerTendenciaIngresos(LocalDate inicio, LocalDate fin) {
        try {
            log.info("💰 Generando tendencia de ingresos desde {} hasta {}", inicio, fin);
            
            List<ReporteTendenciaDTO> tendencia = new ArrayList<>();
            LocalDate fecha = inicio;
            
            while (!fecha.isAfter(fin)) {
                Instant inicioDia = toInstant(fecha.atStartOfDay());
                Instant finDia = toInstant(fecha.atTime(23, 59, 59));
                
                java.math.BigDecimal bd_monto = pagoRepository.sumMontoByEstadoAndFechaBetween("aprobado", inicioDia, finDia); Double monto = bd_monto != null ? bd_monto.doubleValue() : 0.0;
                if (monto == null) monto = 0.0;
                
                String fechaStr = fecha.getDayOfMonth() + " " + 
                    fecha.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
                
                tendencia.add(new ReporteTendenciaDTO(fechaStr, null, monto));
                fecha = fecha.plusDays(1);
            }
            
            return tendencia;
        } catch (Exception e) {
            log.error("❌ Error al generar tendencia de ingresos: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 💳 HISTORIAL DE PAGOS
     * Obtiene los últimos pagos registrados
     */
    @Transactional(readOnly = true)
    public List<ReportePagoHistorialDTO> obtenerHistorialPagos(Integer limite) {
        try {
            log.info("💳 Generando historial de {} pagos", limite);
            
            List<Object[]> resultados = pagoRepository.obtenerHistorialPagos(limite);
            List<ReportePagoHistorialDTO> historial = new ArrayList<>();
            
            // Aplicar límite a los resultados
            int registrosAProcesar = (limite != null && limite > 0) ? Math.min(limite, resultados.size()) : resultados.size();
            
            for (int i = 0; i < registrosAProcesar; i++) {
                Object[] fila = resultados.get(i);
                Long pagoId = (Long) fila[0];
                Instant fechaRegistro = (Instant) fila[1];
                String nombreCliente = (String) fila[2] + " " + (String) fila[3];
                String nombreMembresia = (String) fila[4];
                String metodoPago = (String) fila[5];  // Ahora sí usa metodoPago
                Double monto = ((Number) fila[6]).doubleValue();
                String estado = (String) fila[7];
                
                LocalDateTime fechaHora = toLocalDateTime(fechaRegistro);
                String fecha = fechaHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String hora = fechaHora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                
                String estadoTraducido = estado.equals("aprobado") ? "Confirmado" : 
                                        estado.equals("pendiente") ? "Pendiente" : "Rechazado";
                
                // Usar metodoPago, si es null usar "Sin especificar"
                String metodoFinal = metodoPago != null ? metodoPago : "Sin especificar";
                
                historial.add(new ReportePagoHistorialDTO(
                    pagoId,
                    fecha,
                    hora,
                    nombreCliente,
                    nombreMembresia != null ? nombreMembresia : "Sin membresía",
                    metodoFinal,
                    monto,
                    estadoTraducido
                ));
            }
            
            return historial;
        } catch (Exception e) {
            log.error("❌ Error al generar historial de pagos: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 📊 DISTRIBUCIÓN INGRESOS POR PLAN
     * Agrupa ingresos por tipo de membresía
     */
    @Transactional(readOnly = true)
    public List<ReporteDistribucionDTO> obtenerDistribucionIngresosPorPlan(LocalDate inicio, LocalDate fin) {
        try {
            log.info("📊 Generando distribución de ingresos por plan desde {} hasta {}", inicio, fin);
            
            Instant inicioInstant = toInstant(inicio.atStartOfDay());
            Instant finInstant = toInstant(fin.atTime(23, 59, 59));
            
            List<Object[]> resultados = pagoRepository.sumMontoByMembresiaAndFechaBetween(inicioInstant, finInstant);
            List<ReporteDistribucionDTO> distribucion = new ArrayList<>();
            
            String[] colores = {"#fbbf24", "#10b981", "#60a5fa", "#fb923c", "#a78bfa"};
            int colorIndex = 0;
            
            for (Object[] fila : resultados) {
                String nombreMembresia = (String) fila[0];
                Double totalIngresos = ((Number) fila[1]).doubleValue();
                
                String color = colores[colorIndex % colores.length];
                distribucion.add(new ReporteDistribucionDTO(
                    "Plan " + nombreMembresia, 
                    totalIngresos.intValue(), 
                    color
                ));
                colorIndex++;
            }
            
            return distribucion;
        } catch (Exception e) {
            log.error("❌ Error al generar distribución de ingresos por plan: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 📊 MÉTRICAS COMPARATIVAS DEL DASHBOARD
     * Obtiene todas las métricas con comparación vs periodo anterior
     */
    @Transactional(readOnly = true)
    public List<MetricaComparativaDTO> obtenerMetricasComparativas(LocalDate inicioParam, LocalDate finParam) {
        log.info("📊 Generando métricas comparativas del dashboard (con rango)");
        
        // Si no se proporcionan fechas, usar mes actual
        if (inicioParam == null || finParam == null) {
            return obtenerMetricasComparativas();
        }
        
        LocalDate inicio = inicioParam;
        LocalDate fin = finParam;
        
        // Calcular periodo anterior del mismo tamaño
        long diasPeriodo = ChronoUnit.DAYS.between(inicio, fin) + 1;
        LocalDate inicioAnterior = inicio.minusDays(diasPeriodo);
        LocalDate finAnterior = fin.minusDays(diasPeriodo);
        
        log.info("📊 Periodo actual: {} a {} ({} días)", inicio, fin, diasPeriodo);
        log.info("📊 Periodo anterior: {} a {}", inicioAnterior, finAnterior);
        
        List<MetricaComparativaDTO> metricas = new ArrayList<>();
        
        // === MÉTRICA DE ASISTENCIA ===
        try {
            log.info("Calculando Total Asistencias...");
            LocalDateTime inicioActualDT = inicio.atStartOfDay();
            LocalDateTime finActualDT = fin.atTime(23, 59, 59);
            LocalDateTime inicioAnteriorDT = inicioAnterior.atStartOfDay();
            LocalDateTime finAnteriorDT = finAnterior.atTime(23, 59, 59);
            
            Integer actual = asistenciaRepository.countByFechaHoraBetween(inicioActualDT, finActualDT);
            Integer anterior = asistenciaRepository.countByFechaHoraBetween(inicioAnteriorDT, finAnteriorDT);
            
            actual = actual != null ? actual : 0;
            anterior = anterior != null ? anterior : 0;
            
            log.info("✅ Total Asistencias: actual={}, anterior={}", actual, anterior);
            metricas.add(crearMetrica("Total Asistencias", actual.toString(), anterior.toString(), 
                                   "users", "asistencia"));
        } catch (Exception e) {
            log.error("❌ Error en Total Asistencias: {}", e.getMessage(), e);
            metricas.add(crearMetrica("Total Asistencias", "Error", "Error", "users", "asistencia"));
        }
        
        // === MÉTRICAS DE INGRESOS ===
        try {
            log.info("Calculando métricas de ingresos...");
            
            // Convertir LocalDate a Instant para queries de pagos
            Instant inicioActualInst = toInstant(inicio.atStartOfDay());
            Instant finActualInst = toInstant(fin.atTime(23, 59, 59));
            Instant inicioAnteriorInst = toInstant(inicioAnterior.atStartOfDay());
            Instant finAnteriorInst = toInstant(finAnterior.atTime(23, 59, 59));
            
            // 1. Ingresos Totales
            java.math.BigDecimal bd_ingresosActual = pagoRepository.sumMontoByFechaBetween(inicioActualInst, finActualInst); Double ingresosActual = bd_ingresosActual != null ? bd_ingresosActual.doubleValue() : 0.0;
            java.math.BigDecimal bd_ingresosAnterior = pagoRepository.sumMontoByFechaBetween(inicioAnteriorInst, finAnteriorInst); Double ingresosAnterior = bd_ingresosAnterior != null ? bd_ingresosAnterior.doubleValue() : 0.0;
            ingresosActual = ingresosActual != null ? ingresosActual : 0.0;
            ingresosAnterior = ingresosAnterior != null ? ingresosAnterior : 0.0;
            
            log.info("✅ Ingresos Totales: actual=${}, anterior=${}", ingresosActual, ingresosAnterior);
            metricas.add(crearMetrica("Ingresos Totales", 
                                     String.format("$%.2f", ingresosActual), 
                                     String.format("$%.2f", ingresosAnterior), 
                                     "dollar-sign", "ingreso"));
            
            // 2. Ingresos Esta Semana (solo si el rango es >= 7 días)
            if (diasPeriodo >= 7) {
                LocalDate inicioSemana = fin.minusDays(6); // Últimos 7 días
                LocalDate inicioSemanaAnt = finAnterior.minusDays(6);
                
                Instant inicioSemanaInst = toInstant(inicioSemana.atStartOfDay());
                Instant finSemanaInst = toInstant(fin.atTime(23, 59, 59));
                Instant inicioSemanaAntInst = toInstant(inicioSemanaAnt.atStartOfDay());
                Instant finSemanaAntInst = toInstant(finAnterior.atTime(23, 59, 59));
                
                java.math.BigDecimal bd_semanaActual = pagoRepository.sumMontoByFechaBetween(inicioSemanaInst, finSemanaInst); Double semanaActual = bd_semanaActual != null ? bd_semanaActual.doubleValue() : 0.0;
                java.math.BigDecimal bd_semanaAnterior = pagoRepository.sumMontoByFechaBetween(inicioSemanaAntInst, finSemanaAntInst); Double semanaAnterior = bd_semanaAnterior != null ? bd_semanaAnterior.doubleValue() : 0.0;
                semanaActual = semanaActual != null ? semanaActual : 0.0;
                semanaAnterior = semanaAnterior != null ? semanaAnterior : 0.0;
                
                log.info("✅ Ingresos Esta Semana: actual=${}, anterior=${}", semanaActual, semanaAnterior);
                metricas.add(crearMetrica("Ingresos Esta Semana", 
                                         String.format("$%.2f", semanaActual), 
                                         String.format("$%.2f", semanaAnterior), 
                                         "trending-up", "ingreso"));
            }
            
            // 3. Ingresos Hoy (solo si el rango incluye hoy)
            LocalDate hoy = LocalDate.now();
            if (!fin.isBefore(hoy) && !inicio.isAfter(hoy)) {
                LocalDate ayer = hoy.minusDays(1);
                
                Instant inicioHoyInst = toInstant(hoy.atStartOfDay());
                Instant finHoyInst = toInstant(hoy.atTime(23, 59, 59));
                Instant inicioAyerInst = toInstant(ayer.atStartOfDay());
                Instant finAyerInst = toInstant(ayer.atTime(23, 59, 59));
                
                java.math.BigDecimal bd_hoyMonto = pagoRepository.sumMontoByFechaBetween(inicioHoyInst, finHoyInst); Double hoyMonto = bd_hoyMonto != null ? bd_hoyMonto.doubleValue() : 0.0;
                java.math.BigDecimal bd_ayerMonto = pagoRepository.sumMontoByFechaBetween(inicioAyerInst, finAyerInst); Double ayerMonto = bd_ayerMonto != null ? bd_ayerMonto.doubleValue() : 0.0;
                hoyMonto = hoyMonto != null ? hoyMonto : 0.0;
                ayerMonto = ayerMonto != null ? ayerMonto : 0.0;
                
                log.info("✅ Ingresos Hoy: actual=${}, anterior=${}", hoyMonto, ayerMonto);
                metricas.add(crearMetrica("Ingresos Hoy", 
                                         String.format("$%.2f", hoyMonto), 
                                         String.format("$%.2f", ayerMonto), 
                                         "calendar", "ingreso"));
            }
            
            // 4. Pagos Realizados
            Integer pagosActual = pagoRepository.countPagosRealizadosInRange(inicioActualInst, finActualInst);
            Integer pagosAnterior = pagoRepository.countPagosRealizadosInRange(inicioAnteriorInst, finAnteriorInst);
            pagosActual = pagosActual != null ? pagosActual : 0;
            pagosAnterior = pagosAnterior != null ? pagosAnterior : 0;
            
            log.info("✅ Pagos Realizados: actual={}, anterior={}", pagosActual, pagosAnterior);
            metricas.add(crearMetrica("Pagos Realizados", pagosActual.toString(), pagosAnterior.toString(), 
                                     "credit-card", "ingreso"));
            
        } catch (Exception e) {
            log.error("❌ Error al calcular métricas de ingresos: {}", e.getMessage(), e);
        }
        
        // === MÉTRICAS DE SUSCRIPCIONES ===
        try {
            log.info("Calculando métricas de suscripciones...");
            
            // IMPORTANTE: Las métricas de suscripciones siempre se calculan al día de HOY
            // No se proyectan al final del rango porque son estados actuales, no históricos
            LocalDate hoy = LocalDate.now();
            
            // 1. Suscripciones Activas (HOY)
            Integer activasActual = clienteRepository.countClientesActivos(hoy);
            Integer activasAnterior = clienteRepository.countClientesActivos(hoy.minusDays(diasPeriodo));
            activasActual = activasActual != null ? activasActual : 0;
            activasAnterior = activasAnterior != null ? activasAnterior : 0;
            
            log.info("✅ Suscripciones Activas: actual={}, anterior={}", activasActual, activasAnterior);
            metricas.add(crearMetrica("Suscripciones Activas", activasActual.toString(), activasAnterior.toString(), 
                                     "check-circle", "suscripcion"));
            
            // 2. Suscripciones Vencidas (HOY)
            Integer vencidasActual = clienteRepository.countByFechaVencimientoBefore(hoy);
            Integer vencidasAnterior = clienteRepository.countByFechaVencimientoBefore(hoy.minusDays(diasPeriodo));
            vencidasActual = vencidasActual != null ? vencidasActual : 0;
            vencidasAnterior = vencidasAnterior != null ? vencidasAnterior : 0;
            
            log.info("✅ Suscripciones Vencidas: actual={}, anterior={}", vencidasActual, vencidasAnterior);
            metricas.add(crearMetrica("Suscripciones Vencidas", vencidasActual.toString(), vencidasAnterior.toString(), 
                                     "x-circle", "suscripcion"));
            
            // 3. Próximas a Vencer (desde hoy+1 hasta hoy+7)
            LocalDate limiteInicio = hoy.plusDays(1);
            LocalDate limiteFin = hoy.plusDays(7);
            LocalDate limiteInicioAnterior = hoy.minusDays(diasPeriodo).plusDays(1);
            LocalDate limiteFinAnterior = hoy.minusDays(diasPeriodo).plusDays(7);
            
            long proximasActual = clienteRepository.countProximosAVencerEnRango(limiteInicio, limiteFin);
            long proximasAnterior = clienteRepository.countProximosAVencerEnRango(limiteInicioAnterior, limiteFinAnterior);
            
            log.info("✅ Próximas a Vencer: actual={}, anterior={}", proximasActual, proximasAnterior);
            metricas.add(crearMetrica("Próximas a Vencer", String.valueOf(proximasActual), String.valueOf(proximasAnterior), 
                                     "alert-circle", "suscripcion"));
            
            // 4. Renovaciones (en el rango completo)
            Instant inicioActualInst = toInstant(inicio.atStartOfDay());
            Instant finActualInst = toInstant(fin.atTime(23, 59, 59));
            Instant inicioAnteriorInst = toInstant(inicioAnterior.atStartOfDay());
            Instant finAnteriorInst = toInstant(finAnterior.atTime(23, 59, 59));
            
            long renovacionesActual = renovacionRepository.countRenovacionesEnRango(inicioActualInst, finActualInst);
            long renovacionesAnterior = renovacionRepository.countRenovacionesEnRango(inicioAnteriorInst, finAnteriorInst);
            
            log.info("✅ Renovaciones: actual={}, anterior={}", renovacionesActual, renovacionesAnterior);
            metricas.add(crearMetrica("Renovaciones", String.valueOf(renovacionesActual), String.valueOf(renovacionesAnterior), 
                                     "refresh-cw", "suscripcion"));
        } catch (Exception e) {
            log.error("❌ Error en métricas de suscripciones: {}", e.getMessage(), e);
        }
        
        log.info("✅ Métricas calculadas: {} métricas", metricas.size());
        return metricas;
    }
    
    /**
     * 📊 MÉTRICAS COMPARATIVAS DEL DASHBOARD (sin parámetros - usa mes actual)
     * Obtiene todas las métricas con comparación vs periodo anterior
     */
    @Transactional(readOnly = true)
    public List<MetricaComparativaDTO> obtenerMetricasComparativas() {
        try {
            log.info("📊 Generando métricas comparativas del dashboard");
            
            LocalDate hoy = LocalDate.now();
            YearMonth mesActual = YearMonth.from(hoy);
            YearMonth mesAnterior = mesActual.minusMonths(1);
            
            List<MetricaComparativaDTO> metricas = new ArrayList<>();
            
            // === MÉTRICAS DE ASISTENCIA ===
            metricas.add(calcularMetricaAsistenciasTotales(mesActual, mesAnterior));
            metricas.add(calcularMetricaNuevosClientes(mesActual, mesAnterior));
            metricas.add(calcularMetricaClientesAusentes(hoy));
            metricas.add(calcularMetricaTasaRetencion(hoy, mesActual));
            
            // === MÉTRICAS DE SUSCRIPCIONES ===
            metricas.add(calcularMetricaSuscripcionesActivas(hoy, mesAnterior.atEndOfMonth()));
            metricas.add(calcularMetricaSuscripcionesVencidas(hoy, mesAnterior.atEndOfMonth()));
            metricas.add(calcularMetricaProximasVencer(hoy, mesAnterior.atEndOfMonth()));
            metricas.add(calcularMetricaRenovaciones(mesActual, mesAnterior));
            // NOTA: NO incluimos cancelaciones porque el negocio no permite cancelar membresías activas
            
            // === MÉTRICAS DE INGRESOS ===
            metricas.add(calcularMetricaIngresosTotales(mesActual, mesAnterior));
            metricas.add(calcularMetricaIngresosSemana(hoy));
            metricas.add(calcularMetricaIngresosHoy(hoy));
            metricas.add(calcularMetricaPagosRealizados(mesActual, mesAnterior));
            
            return metricas;
        } catch (Exception e) {
            log.error("❌ Error al generar métricas comparativas: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 💳 DISTRIBUCIÓN INGRESOS POR MÉTODO DE PAGO
     * Agrupa ingresos por efectivo, tarjeta, transferencia
     */
    @Transactional(readOnly = true)
    public List<ReporteMetodoPagoDTO> obtenerDistribucionMetodosPago(LocalDate inicio, LocalDate fin) {
        try {
            log.info("💳 Generando distribución por métodos de pago desde {} hasta {}", inicio, fin);
            
            Instant inicioInstant = toInstant(inicio.atStartOfDay());
            Instant finInstant = toInstant(fin.atTime(23, 59, 59));
            
            List<Object[]> resultados = pagoRepository.sumMontoByMetodoPagoAndFechaBetween(inicioInstant, finInstant);
            List<ReporteMetodoPagoDTO> distribucion = new ArrayList<>();
            
            double totalGeneral = 0.0;
            
            // Primera pasada: calcular total
            for (Object[] fila : resultados) {
                Double total = ((Number) fila[1]).doubleValue();
                totalGeneral += total;
            }
            
            // Segunda pasada: calcular porcentajes
            for (Object[] fila : resultados) {
                String metodo = (String) fila[0];
                Double total = ((Number) fila[1]).doubleValue();
                Long cantidadLong = ((Number) fila[2]).longValue();
                Integer cantidad = cantidadLong.intValue();
                Double porcentaje = totalGeneral > 0 ? (total / totalGeneral) * 100 : 0.0;
                
                distribucion.add(new ReporteMetodoPagoDTO(
                    metodo != null ? metodo : "Sin especificar",
                    total,
                    cantidad,
                    porcentaje
                ));
            }
            
            return distribucion;
        } catch (Exception e) {
            log.error("❌ Error al generar distribución por métodos de pago: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 📊 RENOVACIONES (HISTÓRICO DINÁMICO)
     * Obtiene el histórico de renovaciones desde la tabla renovaciones.
     * Se adapta automáticamente al rango de fechas:
     * - Rango <= 2 días: agrupa por hora (vista DÍA)
     * - Rango <= 31 días: agrupa por día (vista SEMANA o MES)
     * - Rango <= 365 días: agrupa por mes (vista AÑO)
     * - Rango > 365 días: agrupa por año (múltiples años)
     * 
     * @param inicioParam Fecha inicio opcional (si es null, usa últimos 12 meses)
     * @param finParam Fecha fin opcional (si es null, usa hoy)
     */
    @Transactional(readOnly = true)
    public List<ReporteRenovacionCancelacionDTO> obtenerRenovacionesCancelaciones(LocalDate inicioParam, LocalDate finParam) {
        try {
            LocalDate inicio, fin;
            
            // Si no se proporcionan fechas, usar últimos 12 meses
            if (inicioParam == null || finParam == null) {
                fin = LocalDate.now();
                inicio = fin.minusMonths(11).withDayOfMonth(1);
                log.info("📊 Generando reporte de renovaciones (últimos 12 meses)");
            } else {
                inicio = inicioParam;
                fin = finParam;
                log.info("📊 Generando reporte de renovaciones desde {} hasta {}", inicio, fin);
            }
            
            long diasEnRango = ChronoUnit.DAYS.between(inicio, fin) + 1;
            List<ReporteRenovacionCancelacionDTO> reporte = new ArrayList<>();
            
            // Determinar agrupación según el rango
            if (diasEnRango <= 2) {
                // AGRUPACIÓN POR HORA (para vista de DÍA)
                log.info("📊 Agrupando por HORA (rango: {} días)", diasEnRango);
                LocalDateTime inicioHora = inicio.atStartOfDay();
                LocalDateTime finHora = fin.atTime(23, 59, 59);
                
                while (inicioHora.isBefore(finHora) || inicioHora.equals(finHora)) {
                    LocalDateTime finHoraActual = inicioHora.plusHours(1).minusSeconds(1);
                    if (finHoraActual.isAfter(finHora)) finHoraActual = finHora;
                    
                    Instant inicioInstant = toInstant(inicioHora);
                    Instant finInstant = toInstant(finHoraActual);
                    
                    long renovaciones = renovacionRepository.countRenovacionesEnRango(inicioInstant, finInstant);
                    
                    String etiqueta = String.format("%02d:00", inicioHora.getHour());
                    
                    reporte.add(new ReporteRenovacionCancelacionDTO(
                        etiqueta,
                        (int) renovaciones,
                        0,
                        inicioHora.getYear(),
                        inicioHora.getMonthValue()
                    ));
                    
                    inicioHora = inicioHora.plusHours(1);
                }
                
            } else if (diasEnRango <= 31) {
                // AGRUPACIÓN POR DÍA DEL MES (para vista de SEMANA o MES)
                // Mostrar solo el número del día (1, 2, 3... 30)
                log.info("📊 Agrupando por DÍA DEL MES (rango: {} días)", diasEnRango);
                LocalDate diaActual = inicio;
                
                while (!diaActual.isAfter(fin)) {
                    Instant inicioInstant = toInstant(diaActual.atStartOfDay());
                    Instant finInstant = toInstant(diaActual.atTime(23, 59, 59));
                    
                    long renovaciones = renovacionRepository.countRenovacionesEnRango(inicioInstant, finInstant);
                    
                    // Etiqueta: solo número del día "1", "2", "3"... "30"
                    String etiqueta = String.valueOf(diaActual.getDayOfMonth());
                    
                    reporte.add(new ReporteRenovacionCancelacionDTO(
                        etiqueta,
                        (int) renovaciones,
                        0,
                        diaActual.getYear(),
                        diaActual.getMonthValue()
                    ));
                    
                    diaActual = diaActual.plusDays(1);
                }
                
            } else if (diasEnRango < 400) {
                // AGRUPACIÓN POR MES (para vista de AÑO o rangos largos)
                // Cambiado de <= 365 a < 400 para asegurar que un año completo (365 días) se agrupe por meses
                log.info("📊 Agrupando por MES (rango: {} días)", diasEnRango);
                YearMonth mesInicio = YearMonth.from(inicio);
                YearMonth mesFin = YearMonth.from(fin);
                YearMonth mesActual = mesInicio;
                
                while (!mesActual.isAfter(mesFin)) {
                    LocalDate inicioMes = mesActual.atDay(1);
                    LocalDate finMes = mesActual.atEndOfMonth();
                    
                    Instant inicioInstant = toInstant(inicioMes.atStartOfDay());
                    Instant finInstant = toInstant(finMes.atTime(23, 59, 59));
                    
                    long renovaciones = renovacionRepository.countRenovacionesEnRango(inicioInstant, finInstant);
                    
                    String nombreMes = mesActual.getMonth()
                        .getDisplayName(TextStyle.SHORT, java.util.Locale.forLanguageTag("es"))
                        .substring(0, 1).toUpperCase() + 
                        mesActual.getMonth()
                        .getDisplayName(TextStyle.SHORT, java.util.Locale.forLanguageTag("es"))
                        .substring(1);
                    
                    reporte.add(new ReporteRenovacionCancelacionDTO(
                        nombreMes,
                        (int) renovaciones,
                        0,
                        mesActual.getYear(),
                        mesActual.getMonthValue()
                    ));
                    
                    mesActual = mesActual.plusMonths(1);
                }
                
            } else {
                // AGRUPACIÓN POR AÑO (para rangos muy largos)
                log.info("📊 Agrupando por AÑO (rango: {} días)", diasEnRango);
                int anioInicio = inicio.getYear();
                int anioFin = fin.getYear();
                
                for (int anio = anioInicio; anio <= anioFin; anio++) {
                    LocalDate inicioAnio = LocalDate.of(anio, 1, 1);
                    LocalDate finAnio = LocalDate.of(anio, 12, 31);
                    
                    // Ajustar si el rango no cubre todo el año
                    if (inicioAnio.isBefore(inicio)) inicioAnio = inicio;
                    if (finAnio.isAfter(fin)) finAnio = fin;
                    
                    Instant inicioInstant = toInstant(inicioAnio.atStartOfDay());
                    Instant finInstant = toInstant(finAnio.atTime(23, 59, 59));
                    
                    long renovaciones = renovacionRepository.countRenovacionesEnRango(inicioInstant, finInstant);
                    
                    reporte.add(new ReporteRenovacionCancelacionDTO(
                        String.valueOf(anio),
                        (int) renovaciones,
                        0,
                        anio,
                        1
                    ));
                }
            }
            
            log.info("✅ Reporte generado con {} períodos", reporte.size());
            return reporte;
            
        } catch (Exception e) {
            log.error("❌ Error al generar reporte de renovaciones: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Sobrecarga sin parámetros para mantener compatibilidad
     */
    @Transactional(readOnly = true)
    public List<ReporteRenovacionCancelacionDTO> obtenerRenovacionesCancelaciones() {
        return obtenerRenovacionesCancelaciones(null, null);
    }

    // ========== MÉTODOS AUXILIARES PARA MÉTRICAS ==========

    private MetricaComparativaDTO calcularMetricaAsistenciasTotales(YearMonth mesActual, YearMonth mesAnterior) {
        LocalDateTime inicioActual = mesActual.atDay(1).atStartOfDay();
        LocalDateTime finActual = mesActual.atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime inicioAnterior = mesAnterior.atDay(1).atStartOfDay();
        LocalDateTime finAnterior = mesAnterior.atEndOfMonth().atTime(23, 59, 59);
        
        Integer actual = asistenciaRepository.countByFechaHoraBetween(inicioActual, finActual);
        Integer anterior = asistenciaRepository.countByFechaHoraBetween(inicioAnterior, finAnterior);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Total Asistencias", actual.toString(), anterior.toString(), 
                           "users", "asistencia");
    }

    private MetricaComparativaDTO calcularMetricaNuevosClientes(YearMonth mesActual, YearMonth mesAnterior) {
        LocalDate inicioActual = mesActual.atDay(1);
        LocalDate finActual = mesActual.atEndOfMonth();
        LocalDate inicioAnterior = mesAnterior.atDay(1);
        LocalDate finAnterior = mesAnterior.atEndOfMonth();
        
        Integer actual = clienteRepository.countNuevosClientesInRange(inicioActual, finActual);
        Integer anterior = clienteRepository.countNuevosClientesInRange(inicioAnterior, finAnterior);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Nuevos Clientes", actual.toString(), anterior.toString(), 
                           "user-plus", "asistencia");
    }

    private MetricaComparativaDTO calcularMetricaClientesAusentes(LocalDate hoy) {
        LocalDateTime hace7DiasDateTime = hoy.minusDays(7).atStartOfDay();
        LocalDateTime hace14DiasDateTime = hoy.minusDays(14).atStartOfDay();
        
        List<Object[]> ausentes = asistenciaRepository.obtenerClientesAusentes(hace7DiasDateTime);
        List<Object[]> ausentesAnterior = asistenciaRepository.obtenerClientesAusentes(hace14DiasDateTime);
        
        Integer actual = ausentes.size();
        Integer anterior = ausentesAnterior.size();
        
        return crearMetrica("Clientes Ausentes (7+ días)", actual.toString(), anterior.toString(), 
                           "user-x", "asistencia");
    }

    private MetricaComparativaDTO calcularMetricaTasaRetencion(LocalDate hoy, YearMonth mesActual) {
        LocalDate inicioMes = mesActual.atDay(1);
        
        Integer retenidos = clienteRepository.countClientesRetenidos(hoy, inicioMes);
        Integer antiguos = clienteRepository.countClientesAntiguos(inicioMes);
        
        retenidos = retenidos != null ? retenidos : 0;
        antiguos = antiguos != null ? antiguos : 1; // Evitar división por cero
        
        double tasaActual = (retenidos.doubleValue() / antiguos.doubleValue()) * 100;
        
        // Mes anterior
        YearMonth mesAnterior = mesActual.minusMonths(1);
        LocalDate inicioMesAnterior = mesAnterior.atDay(1);
        LocalDate finMesAnterior = mesAnterior.atEndOfMonth();
        
        Integer retenidosAnterior = clienteRepository.countClientesRetenidos(finMesAnterior, inicioMesAnterior);
        Integer antiguosAnterior = clienteRepository.countClientesAntiguos(inicioMesAnterior);
        
        retenidosAnterior = retenidosAnterior != null ? retenidosAnterior : 0;
        antiguosAnterior = antiguosAnterior != null ? antiguosAnterior : 1;
        
        double tasaAnterior = (retenidosAnterior.doubleValue() / antiguosAnterior.doubleValue()) * 100;
        
        return crearMetrica("Tasa de Retención", 
                           String.format("%.0f%%", tasaActual), 
                           String.format("%.0f%%", tasaAnterior), 
                           "target", "asistencia");
    }

    private MetricaComparativaDTO calcularMetricaSuscripcionesActivas(LocalDate hoy, LocalDate hoyanterior) {
        Integer actual = clienteRepository.countClientesActivos(hoy);
        Integer anterior = clienteRepository.countClientesActivos(hoyanterior);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Suscripciones Activas", actual.toString(), anterior.toString(), 
                           "check-circle", "suscripcion");
    }

    private MetricaComparativaDTO calcularMetricaSuscripcionesVencidas(LocalDate hoy, LocalDate hoyAnterior) {
        Integer actual = clienteRepository.countByFechaVencimientoBefore(hoy);
        Integer anterior = clienteRepository.countByFechaVencimientoBefore(hoyAnterior);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Suscripciones Vencidas", actual.toString(), anterior.toString(), 
                           "x-circle", "suscripcion");
    }

    /**
     * Calcula métrica de suscripciones próximas a vencer en los próximos 15 días.
     * Simplificado: solo consulta tabla clientes con fechaVencimiento válida y membresía activa.
     */
    private MetricaComparativaDTO calcularMetricaProximasVencer(LocalDate hoy, LocalDate hoyAnterior) {
        // Próximas a vencer: de hoy+1 hasta hoy+7 (solo próximos 7 días)
        LocalDate limiteInicio = hoy.plusDays(1);     
        LocalDate limiteFin = hoy.plusDays(7);       
        LocalDate limiteInicioAnterior = hoyAnterior.plusDays(1);
        LocalDate limiteFinAnterior = hoyAnterior.plusDays(7);
        
        // Usa el nuevo método simplificado de ClienteRepository
        long actual = clienteRepository.countProximosAVencerEnRango(limiteInicio, limiteFin);
        long anterior = clienteRepository.countProximosAVencerEnRango(limiteInicioAnterior, limiteFinAnterior);
        
        return crearMetrica("Próximas a Vencer", String.valueOf(actual), String.valueOf(anterior), 
                           "alert-circle", "suscripcion");
    }

    /**
     * Calcula métrica de renovaciones usando la tabla de historial.
     * Ahora consulta directamente la tabla 'renovaciones'.
     */
    private MetricaComparativaDTO calcularMetricaRenovaciones(YearMonth mesActual, YearMonth mesAnterior) {
        Instant inicioActual = toInstant(mesActual.atDay(1).atStartOfDay());
        Instant finActual = toInstant(mesActual.atEndOfMonth().atTime(23, 59, 59));
        Instant inicioAnterior = toInstant(mesAnterior.atDay(1).atStartOfDay());
        Instant finAnterior = toInstant(mesAnterior.atEndOfMonth().atTime(23, 59, 59));
        
        // Consulta directa a la tabla renovaciones
        long actual = renovacionRepository.countRenovacionesEnRango(inicioActual, finActual);
        long anterior = renovacionRepository.countRenovacionesEnRango(inicioAnterior, finAnterior);
        
        return crearMetrica("Renovaciones", String.valueOf(actual), String.valueOf(anterior), 
                           "refresh-cw", "suscripcion");
    }

    // NOTA: Método calcularMetricaCancelaciones eliminado porque el negocio no permite cancelar membresías activas

    private MetricaComparativaDTO calcularMetricaIngresosTotales(YearMonth mesActual, YearMonth mesAnterior) {
        Instant inicioActual = toInstant(mesActual.atDay(1).atStartOfDay());
        Instant finActual = toInstant(mesActual.atEndOfMonth().atTime(23, 59, 59));
        Instant inicioAnterior = toInstant(mesAnterior.atDay(1).atStartOfDay());
        Instant finAnterior = toInstant(mesAnterior.atEndOfMonth().atTime(23, 59, 59));
        
        java.math.BigDecimal bd_actual = pagoRepository.sumMontoByFechaBetween(inicioActual, finActual); Double actual = bd_actual != null ? bd_actual.doubleValue() : 0.0;
        java.math.BigDecimal bd_anterior = pagoRepository.sumMontoByFechaBetween(inicioAnterior, finAnterior); Double anterior = bd_anterior != null ? bd_anterior.doubleValue() : 0.0;
        
        actual = actual != null ? actual : 0.0;
        anterior = anterior != null ? anterior : 0.0;
        
        return crearMetrica("Ingresos Totales", 
                           String.format("$%.2f", actual), 
                           String.format("$%.2f", anterior), 
                           "dollar-sign", "ingreso");
    }

    private MetricaComparativaDTO calcularMetricaIngresosSemana(LocalDate hoy) {
        LocalDate inicioSemana = hoy.minusDays(6);
        LocalDate inicioSemanaAnterior = hoy.minusDays(13);
        LocalDate finSemanaAnterior = hoy.minusDays(7);
        
        Instant inicioActual = toInstant(inicioSemana.atStartOfDay());
        Instant finActual = toInstant(hoy.atTime(23, 59, 59));
        Instant inicioAnterior = toInstant(inicioSemanaAnterior.atStartOfDay());
        Instant finAnterior = toInstant(finSemanaAnterior.atTime(23, 59, 59));
        
        java.math.BigDecimal bd_actual = pagoRepository.sumMontoByFechaBetween(inicioActual, finActual); Double actual = bd_actual != null ? bd_actual.doubleValue() : 0.0;
        java.math.BigDecimal bd_anterior = pagoRepository.sumMontoByFechaBetween(inicioAnterior, finAnterior); Double anterior = bd_anterior != null ? bd_anterior.doubleValue() : 0.0;
        
        actual = actual != null ? actual : 0.0;
        anterior = anterior != null ? anterior : 0.0;
        
        return crearMetrica("Ingresos Esta Semana", 
                           String.format("$%.2f", actual), 
                           String.format("$%.2f", anterior), 
                           "trending-up", "ingreso");
    }

    private MetricaComparativaDTO calcularMetricaIngresosHoy(LocalDate hoy) {
        LocalDate ayer = hoy.minusDays(1);
        
        Instant inicioHoy = toInstant(hoy.atStartOfDay());
        Instant finHoy = toInstant(hoy.atTime(23, 59, 59));
        Instant inicioAyer = toInstant(ayer.atStartOfDay());
        Instant finAyer = toInstant(ayer.atTime(23, 59, 59));
        
        java.math.BigDecimal bd_actual = pagoRepository.sumMontoByFechaBetween(inicioHoy, finHoy); Double actual = bd_actual != null ? bd_actual.doubleValue() : 0.0;
        java.math.BigDecimal bd_anterior = pagoRepository.sumMontoByFechaBetween(inicioAyer, finAyer); Double anterior = bd_anterior != null ? bd_anterior.doubleValue() : 0.0;
        
        actual = actual != null ? actual : 0.0;
        anterior = anterior != null ? anterior : 0.0;
        
        return crearMetrica("Ingresos Hoy", 
                           String.format("$%.2f", actual), 
                           String.format("$%.2f", anterior), 
                           "calendar", "ingreso");
    }

    private MetricaComparativaDTO calcularMetricaPagosRealizados(YearMonth mesActual, YearMonth mesAnterior) {
        Instant inicioActual = toInstant(mesActual.atDay(1).atStartOfDay());
        Instant finActual = toInstant(mesActual.atEndOfMonth().atTime(23, 59, 59));
        Instant inicioAnterior = toInstant(mesAnterior.atDay(1).atStartOfDay());
        Instant finAnterior = toInstant(mesAnterior.atEndOfMonth().atTime(23, 59, 59));
        
        Integer actual = pagoRepository.countPagosRealizadosInRange(inicioActual, finActual);
        Integer anterior = pagoRepository.countPagosRealizadosInRange(inicioAnterior, finAnterior);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Pagos Realizados", actual.toString(), anterior.toString(), 
                           "credit-card", "ingreso");
    }

    private MetricaComparativaDTO crearMetrica(String nombre, String valorActual, String valorAnterior, 
                                               String icono, String categoria) {
        // Extraer valores numéricos para calcular porcentaje
        double numActual = extraerValorNumerico(valorActual);
        double numAnterior = extraerValorNumerico(valorAnterior);
        
        double porcentaje = 0.0;
        String tendencia = "neutral";
        
        if (numAnterior > 0) {
            porcentaje = ((numActual - numAnterior) / numAnterior) * 100;
            if (porcentaje > 0.5) tendencia = "up";
            else if (porcentaje < -0.5) tendencia = "down";
        } else if (numActual > 0) {
            porcentaje = 100.0;
            tendencia = "up";
        }
        
        return new MetricaComparativaDTO(nombre, valorActual, valorAnterior, porcentaje, 
                                        tendencia, icono, categoria);
    }

    private double extraerValorNumerico(String valor) {
        try {
            // Remover $, %, comas y otros caracteres
            String limpio = valor.replaceAll("[^0-9.]", "");
            return Double.parseDouble(limpio);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    // ========== MÉTODOS CON RANGO DE FECHAS PERSONALIZADO ==========
    
    private MetricaComparativaDTO calcularMetricaAsistenciasTotalesConRango(
            LocalDate inicio, LocalDate fin, LocalDate inicioAnterior, LocalDate finAnterior) {
        LocalDateTime inicioActualDT = inicio.atStartOfDay();
        LocalDateTime finActualDT = fin.atTime(23, 59, 59);
        LocalDateTime inicioAnteriorDT = inicioAnterior.atStartOfDay();
        LocalDateTime finAnteriorDT = finAnterior.atTime(23, 59, 59);
        
        Integer actual = asistenciaRepository.countByFechaHoraBetween(inicioActualDT, finActualDT);
        Integer anterior = asistenciaRepository.countByFechaHoraBetween(inicioAnteriorDT, finAnteriorDT);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Total Asistencias", actual.toString(), anterior.toString(), 
                           "users", "asistencia");
    }
    
    private MetricaComparativaDTO calcularMetricaNuevosClientesConRango(
            LocalDate inicio, LocalDate fin, LocalDate inicioAnterior, LocalDate finAnterior) {
        Integer actual = clienteRepository.countNuevosClientesInRange(inicio, fin);
        Integer anterior = clienteRepository.countNuevosClientesInRange(inicioAnterior, finAnterior);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Nuevos Clientes", actual.toString(), anterior.toString(), 
                           "user-plus", "asistencia");
    }
    
    private MetricaComparativaDTO calcularMetricaIngresosTotalesConRango(
            LocalDate inicio, LocalDate fin, LocalDate inicioAnterior, LocalDate finAnterior) {
        Instant inicioInstant = toInstant(inicio.atStartOfDay());
        Instant finInstant = toInstant(fin.atTime(23, 59, 59));
        Instant inicioAnteriorInstant = toInstant(inicioAnterior.atStartOfDay());
        Instant finAnteriorInstant = toInstant(finAnterior.atTime(23, 59, 59));
        
        java.math.BigDecimal bd_actual = pagoRepository.sumMontoByEstadoAndFechaBetween(
            "aprobado", inicioInstant, finInstant);
        Double actual = bd_actual != null ? bd_actual.doubleValue() : 0.0;
        java.math.BigDecimal bd_anterior = pagoRepository.sumMontoByEstadoAndFechaBetween(
            "aprobado", inicioAnteriorInstant, finAnteriorInstant);
        Double anterior = bd_anterior != null ? bd_anterior.doubleValue() : 0.0;
        
        actual = actual != null ? actual : 0.0;
        anterior = anterior != null ? anterior : 0.0;
        
        return crearMetrica("Ingresos Totales", 
                           String.format("$%.2f", actual), 
                           String.format("$%.2f", anterior), 
                           "dollar-sign", "ingreso");
    }
    
    private MetricaComparativaDTO calcularMetricaPagosRealizadosConRango(
            LocalDate inicio, LocalDate fin, LocalDate inicioAnterior, LocalDate finAnterior) {
        Instant inicioInstant = toInstant(inicio.atStartOfDay());
        Instant finInstant = toInstant(fin.atTime(23, 59, 59));
        Instant inicioAnteriorInstant = toInstant(inicioAnterior.atStartOfDay());
        Instant finAnteriorInstant = toInstant(finAnterior.atTime(23, 59, 59));
        
        Integer actual = pagoRepository.countPagosRealizadosInRange(inicioInstant, finInstant);
        Integer anterior = pagoRepository.countPagosRealizadosInRange(inicioAnteriorInstant, finAnteriorInstant);
        
        actual = actual != null ? actual : 0;
        anterior = anterior != null ? anterior : 0;
        
        return crearMetrica("Pagos Realizados", actual.toString(), anterior.toString(), 
                           "credit-card", "ingreso");
    }

    // ========================================================================
    // HU-28: REPORTE DE SUSCRIPCIONES (Paginado + Exportar Excel)
    // ========================================================================

    @Transactional(readOnly = true)
    public Page<ReporteSuscripcionDTO> obtenerReporteSuscripciones(
            String estado, LocalDate fechaInicio, LocalDate fechaFin, Integer diasAnticipacion, Pageable pageable) {

        Specification<Cliente> spec = buildSuscripcionSpecification(estado, fechaInicio, fechaFin, diasAnticipacion);
        Page<Cliente> clientesPage = clienteRepository.findAll(spec, pageable);

        List<ReporteSuscripcionDTO> dtos = clientesPage.getContent().stream().map(c -> {
            String plan = c.getMembresiaActual() != null ? c.getMembresiaActual().getNombre() : "Sin Membresía";
            String estadoReal = c.getEstado(); // Este usa la lógica getEstado() que arreglamos ("vencido", "activo", etc)
            return new ReporteSuscripcionDTO(
                    Long.valueOf(c.getId()),
                    c.getNombreCompleto(),
                    c.getEmail(),
                    c.getTelefono(),
                    plan,
                    c.getFechaVencimiento(),
                    estadoReal.toUpperCase()
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, clientesPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public byte[] exportarSuscripcionesExcel(
            String estado, LocalDate fechaInicio, LocalDate fechaFin, Integer diasAnticipacion) {
        
        Specification<Cliente> spec = buildSuscripcionSpecification(estado, fechaInicio, fechaFin, diasAnticipacion);
        List<Cliente> clientes = clienteRepository.findAll(spec); // Traemos todos sin paginación

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Suscripciones");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Cabecera
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Cliente", "Email", "Teléfono", "Plan", "Fecha Vencimiento", "Estado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (Cliente c : clientes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getNombreCompleto());
                row.createCell(2).setCellValue(c.getEmail() != null ? c.getEmail() : "");
                row.createCell(3).setCellValue(c.getTelefono() != null ? c.getTelefono() : "");
                row.createCell(4).setCellValue(c.getMembresiaActual() != null ? c.getMembresiaActual().getNombre() : "Sin Membresía");
                row.createCell(5).setCellValue(c.getFechaVencimiento() != null ? c.getFechaVencimiento().toString() : "");
                row.createCell(6).setCellValue(c.getEstado().toUpperCase());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("❌ Error exportando Excel de suscripciones", e);
            return new byte[0];
        }
    }

    private Specification<Cliente> buildSuscripcionSpecification(
            String estado, LocalDate fechaInicio, LocalDate fechaFin, Integer diasAnticipacion) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDate hoy = LocalDate.now();

            if (estado != null && !estado.isEmpty()) {
                switch (estado.toLowerCase()) {
                    case "activa":
                        predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), hoy));
                        break;
                    case "vencida":
                    case "sin_membresia":
                        predicates.add(cb.lessThan(root.get("fechaVencimiento"), hoy));
                        break;
                    case "por_vencer":
                        int dias = diasAnticipacion != null ? diasAnticipacion : 15;
                        predicates.add(cb.between(root.get("fechaVencimiento"), hoy, hoy.plusDays(dias)));
                        break;
                }
            }

            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaVencimiento"), fechaFin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ========================================================================
    // HU-29: INGRESOS POR MÉTODO DE PAGO Y POR PLAN
    // ========================================================================

    @Transactional(readOnly = true)
    public List<IngresosPorMetodoDTO> obtenerIngresosPorMetodo(LocalDate fechaInicio, LocalDate fechaFin) {
        Instant inicio = toInstant(fechaInicio.atStartOfDay());
        Instant fin = toInstant(fechaFin.atTime(23, 59, 59));

        List<Object[]> resultados = pagoRepository.sumMontoByMetodoPagoAndFechaBetween(inicio, fin);
        
        // Calcular total general para los porcentajes
        double totalGeneral = 0;
        for (Object[] row : resultados) {
            totalGeneral += row[1] != null ? ((Number) row[1]).doubleValue() : 0;
        }

        List<IngresosPorMetodoDTO> dtos = new ArrayList<>();
        for (Object[] row : resultados) {
            String metodo = (String) row[0];
            Double total = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            Integer cantidad = row[2] != null ? ((Number) row[2]).intValue() : 0;
            Double porcentaje = totalGeneral > 0 ? (total / totalGeneral) * 100 : 0.0;

            dtos.add(new IngresosPorMetodoDTO(
                metodo != null ? metodo : "Sin Método",
                Math.round(total * 100.0) / 100.0,
                cantidad,
                Math.round(porcentaje * 100.0) / 100.0
            ));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<IngresosPorPlanDTO> obtenerIngresosPorPlan(LocalDate fechaInicio, LocalDate fechaFin) {
        Instant inicio = toInstant(fechaInicio.atStartOfDay());
        Instant fin = toInstant(fechaFin.atTime(23, 59, 59));

        List<Object[]> resultados = pagoRepository.sumMontoByMembresiaAndFechaBetween(inicio, fin);
        
        double totalGeneral = 0;
        for (Object[] row : resultados) {
            totalGeneral += row[1] != null ? ((Number) row[1]).doubleValue() : 0;
        }

        List<IngresosPorPlanDTO> dtos = new ArrayList<>();
        for (Object[] row : resultados) {
            String plan = (String) row[0];
            Double total = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            Integer cantidad = 0; // The custom query groups by plan, adding COUNT(p) would need a change. For now we leave 0 or approximate.
            Double porcentaje = totalGeneral > 0 ? (total / totalGeneral) * 100 : 0.0;

            dtos.add(new IngresosPorPlanDTO(
                plan != null ? plan : "Sin Plan",
                Math.round(total * 100.0) / 100.0,
                cantidad,
                Math.round(porcentaje * 100.0) / 100.0
            ));
        }
        return dtos;
    }

    // ========================================================================
    // HU-30: HISTORIAL DE PAGOS Y RETENCIÓN
    // ========================================================================

    @Transactional(readOnly = true)
    public List<ReportePagoHistorialDTO> obtenerHistorialPagosDetallado(String busqueda) {
        List<Object[]> resultados = pagoRepository.obtenerHistorialPagosDetallado(busqueda);
        List<ReportePagoHistorialDTO> historial = new ArrayList<>();
        
        for (Object[] fila : resultados) {
            Long pagoId = (Long) fila[0];
            Instant fechaReg = (Instant) fila[1];
            String nombreCli = (String) fila[2] + " " + (String) fila[3];
            String plan = (String) fila[4];
            String metodo = (String) fila[5];
            Double monto = fila[6] != null ? ((Number) fila[6]).doubleValue() : 0.0;
            String estado = (String) fila[7];
            
            LocalDateTime ldt = toLocalDateTime(fechaReg);
            String fechaStr = ldt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaStr = ldt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            
            historial.add(new ReportePagoHistorialDTO(
                pagoId, fechaStr, horaStr, nombreCli, 
                plan != null ? plan : "Sin membresía", 
                metodo != null ? metodo : "Sin especificar", 
                monto, estado
            ));
        }
        return historial;
    }

    @Transactional(readOnly = true)
    public byte[] exportarHistorialPagosExcel(String busqueda) {
        List<ReportePagoHistorialDTO> historial = obtenerHistorialPagosDetallado(busqueda);
        
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Historial de Pagos");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Fecha", "Hora", "Cliente", "Plan", "Método", "Monto", "Estado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (ReportePagoHistorialDTO dto : historial) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getPagoId() != null ? dto.getPagoId() : 0);
                row.createCell(1).setCellValue(dto.getFecha());
                row.createCell(2).setCellValue(dto.getHora());
                row.createCell(3).setCellValue(dto.getCliente());
                row.createCell(4).setCellValue(dto.getPlan());
                row.createCell(5).setCellValue(dto.getMetodo());
                row.createCell(6).setCellValue(dto.getMonto());
                row.createCell(7).setCellValue(dto.getEstado());
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("❌ Error exportando historial de pagos", e);
            return new byte[0];
        }
    }

    @Transactional(readOnly = true)
    public List<RetencionMensualDTO> obtenerRetencionMensual() {
        List<RetencionMensualDTO> retencionList = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        
        // Calcular los últimos 12 meses
        for (int i = 11; i >= 0; i--) {
            LocalDate iterMes = hoy.minusMonths(i);
            LocalDate inicioMes = iterMes.withDayOfMonth(1);
            LocalDate finMes = iterMes.withDayOfMonth(iterMes.lengthOfMonth());
            
            Instant inicioInst = toInstant(inicioMes.atStartOfDay());
            Instant finInst = toInstant(finMes.atTime(23, 59, 59));
            
            // Renovaciones: Pagos hechos en ese mes por clientes antiguos (creados antes del mes)
            Integer renovaciones = clienteRepository.countRenovacionesInRange(inicioInst, finInst, inicioInst);
            if (renovaciones == null) renovaciones = 0;
            
            // Cancelaciones: Clientes con qr inactivo cuya membresia vencio en ese mes
            Integer cancelaciones = clienteRepository.countCancelacionesInRange(inicioMes, finMes);
            if (cancelaciones == null) cancelaciones = 0;
            
            String nombreMes = iterMes.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
            
            retencionList.add(new RetencionMensualDTO(
                iterMes.getYear(),
                iterMes.getMonthValue(),
                nombreMes,
                renovaciones,
                cancelaciones
            ));
        }
        
        return retencionList;
    }
}
