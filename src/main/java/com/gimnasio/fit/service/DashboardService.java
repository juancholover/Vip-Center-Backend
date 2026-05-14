package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.*;
import com.gimnasio.fit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final PagoRepository pagoRepository;
    private final AsistenciaRepository asistenciaRepository;

    /**
     * Convierte LocalDateTime a Instant usando la zona horaria del sistema.
     */
    private Instant toInstant(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Obtiene las estadísticas principales del dashboard.
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO obtenerEstadisticas() {
        try {
            LocalDate hoy = LocalDate.now();
            
            // Clientes activos (fecha_vencimiento >= hoy AND qr_activo = true)
            Integer clientesActivos = clienteRepository.countClientesActivos(hoy);
            if (clientesActivos == null) clientesActivos = 0;

            // Ingresos del mes actual
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
            BigDecimal ingresosMesBd = pagoRepository.sumMontoByFechaBetween(
                toInstant(inicioMes.atStartOfDay()), 
                toInstant(finMes.atTime(23, 59, 59))
            );
            Double ingresosMes = (ingresosMesBd != null) ? ingresosMesBd.doubleValue() : 0.0;

            // Asistencias del día actual
            Integer asistenciasHoy = asistenciaRepository.countByFechaHoraBetween(
                hoy.atStartOfDay(), 
                hoy.atTime(23, 59, 59)
            );
            if (asistenciasHoy == null) asistenciasHoy = 0;

            // Membresías que vencen en los próximos 7 días
            LocalDate dentroSieteDias = hoy.plusDays(7);
            Integer membresiasPorVencer = clienteRepository.countByFechaVencimientoBetween(
                hoy, 
                dentroSieteDias
            );
            if (membresiasPorVencer == null) membresiasPorVencer = 0;

            // Promedio de asistencias diarias del mes actual
            Integer totalAsistenciasMes = asistenciaRepository.countByFechaHoraBetween(
                inicioMes.atStartOfDay(),
                finMes.atTime(23, 59, 59)
            );
            if (totalAsistenciasMes == null) totalAsistenciasMes = 0;
            
            // Calcular promedio: total asistencias / días transcurridos del mes
            int diasTranscurridos = hoy.getDayOfMonth();
            Integer promedioDiario = diasTranscurridos > 0 ? totalAsistenciasMes / diasTranscurridos : 0;

            return new DashboardStatsDTO(clientesActivos, ingresosMes, asistenciasHoy, membresiasPorVencer, promedioDiario);
        } catch (Exception e) {
            log.error("❌ Error al obtener estadísticas del dashboard: {}", e.getMessage(), e);
            // Retornar datos vacíos en caso de error
            return new DashboardStatsDTO(0, 0.0, 0, 0, 0);
        }
    }

    /**
     * Obtiene los ingresos de los últimos 7 días.
     */
    @Transactional(readOnly = true)
    public List<IngresosDiaDTO> obtenerIngresosSemana() {
        List<IngresosDiaDTO> resultado = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            Instant inicio = toInstant(fecha.atStartOfDay());
            Instant fin = toInstant(fecha.atTime(23, 59, 59));

            BigDecimal ingresosBd = pagoRepository.sumMontoByFechaBetween(inicio, fin);
            Double ingresos = (ingresosBd != null) ? ingresosBd.doubleValue() : 0.0;

            // Formato: "Lun", "Mar", etc.
            String nombreDia = fecha.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));

            resultado.add(new IngresosDiaDTO(nombreDia, ingresos));
        }

        return resultado;
    }

    /**
     * Obtiene las asistencias agrupadas por hora del día actual.
     */
    @Transactional(readOnly = true)
    public List<AsistenciasPorHoraDTO> obtenerAsistenciasPorHora() {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDateTime inicioHoy = hoy.atStartOfDay();
            LocalDateTime finHoy = hoy.atTime(23, 59, 59);

            // Obtener todas las asistencias de hoy
            var asistencias = asistenciaRepository.findByFechaHoraBetween(inicioHoy, finHoy);
            if (asistencias == null) {
                asistencias = new ArrayList<>();
            }

            // Agrupar por hora
            List<AsistenciasPorHoraDTO> resultado = new ArrayList<>();
            
            for (int hora = 6; hora <= 22; hora += 2) { // Cada 2 horas desde las 6 AM hasta las 10 PM
                int horaFinal = hora;
                long cantidad = asistencias.stream()
                    .filter(a -> a != null && a.getFechaHora() != null)
                    .filter(a -> a.getFechaHora().getHour() >= horaFinal && a.getFechaHora().getHour() < horaFinal + 2)
                    .count();

                String horaStr = String.format("%02d:00", hora);
                resultado.add(new AsistenciasPorHoraDTO(horaStr, (int) cantidad));
            }

            return resultado;
        } catch (Exception e) {
            log.error("❌ Error al obtener asistencias por hora: {}", e.getMessage(), e);
            // Retornar lista vacía con estructura de horas
            List<AsistenciasPorHoraDTO> resultado = new ArrayList<>();
            for (int hora = 6; hora <= 22; hora += 2) {
                resultado.add(new AsistenciasPorHoraDTO(String.format("%02d:00", hora), 0));
            }
            return resultado;
        }
    }

    /**
     * Obtiene las últimas 10 actividades recientes (pagos, registros, asistencias).
     */
    @Transactional(readOnly = true)
    public List<ActividadRecienteDTO> obtenerActividadReciente() {
        List<ActividadRecienteDTO> actividades = new ArrayList<>();

        try {
            // Últimos 5 pagos aprobados
            var ultimosPagos = pagoRepository.findTop5ByEstadoInOrderByFechaRegistroDesc(java.util.Arrays.asList("aprobado", "approved", "COMPLETADO", "COMPLETED"));
            if (ultimosPagos != null) {
                for (var pago : ultimosPagos) {
                    if (pago == null || pago.getCliente() == null) continue;
                    
                    String nombreCliente = pago.getCliente().getNombreCompleto();
                    if (nombreCliente == null || nombreCliente.isBlank()) {
                        nombreCliente = "Cliente Anónimo";
                    }
                    
                    String mensaje = String.format("%s realizó un pago de S/ %.2f", 
                        nombreCliente, 
                        pago.getMontoFinal() != null ? pago.getMontoFinal() : 0.0
                    );
                    String tiempo = calcularTiempoRelativo(pago.getFechaRegistro());
                    actividades.add(new ActividadRecienteDTO(
                        pago.getId(), 
                        "pago", 
                        mensaje, 
                        tiempo, 
                        "💰"
                    ));
                }
            }

            // Últimas 5 asistencias
            var ultimasAsistencias = asistenciaRepository.findTop5ByOrderByFechaHoraDesc();
            if (ultimasAsistencias != null) {
                for (var asistencia : ultimasAsistencias) {
                    if (asistencia == null || asistencia.getCliente() == null) continue;
                    
                    String nombreCliente = asistencia.getCliente().getNombreCompleto();
                    if (nombreCliente == null || nombreCliente.isBlank()) {
                        nombreCliente = "Cliente Anónimo";
                    }
                    
                    String mensaje = String.format("%s registró su asistencia", nombreCliente);
                    String tiempo = calcularTiempoRelativo(asistencia.getFechaHora());
                    actividades.add(new ActividadRecienteDTO(
                        asistencia.getId(), 
                        "asistencia", 
                        mensaje, 
                        tiempo, 
                        "✅"
                    ));
                }
            }
        } catch (Exception e) {
            log.error("❌ Error al obtener actividad reciente: {}", e.getMessage(), e);
            // Retornar lista vacía en caso de error
            return new ArrayList<>();
        }

        // Ordenar por fecha (más recientes primero) y limitar a 10
        return actividades.stream()
            .sorted((a, b) -> b.getId().compareTo(a.getId()))
            .limit(10)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene la tendencia de asistencias de los últimos N días (HU-26).
     * Agrupa las asistencias totales por fecha.
     * 
     * @param dias Cantidad de días hacia atrás (7 o 30)
     * @return Lista de AsistenciaTendenciaDTO con fecha y cantidad
     */
    @Transactional(readOnly = true)
    public List<AsistenciaTendenciaDTO> obtenerTendenciaAsistencias(int dias) {
        try {
            List<AsistenciaTendenciaDTO> resultado = new ArrayList<>();
            LocalDate hoy = LocalDate.now();

            for (int i = dias - 1; i >= 0; i--) {
                LocalDate fecha = hoy.minusDays(i);
                LocalDateTime inicio = fecha.atStartOfDay();
                LocalDateTime fin = fecha.atTime(23, 59, 59);

                Integer cantidad = asistenciaRepository.countByFechaHoraBetween(inicio, fin);
                if (cantidad == null) cantidad = 0;

                // Formato corto: "Lun", "Mar", etc.
                String nombreDia = fecha.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));

                resultado.add(new AsistenciaTendenciaDTO(nombreDia, cantidad));
            }

            return resultado;
        } catch (Exception e) {
            log.error("❌ Error al obtener tendencia de asistencias: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene el ranking de los clientes con más asistencias del mes actual (HU-26).
     * 
     * @param limite Cantidad máxima de clientes a retornar (ej: 5 o 10)
     * @return Lista de TopClienteDTO con id, nombre y asistencias
     */
    @Transactional(readOnly = true)
    public List<TopClienteDTO> obtenerTopClientes(int limite) {
        try {
            LocalDate hoy = LocalDate.now();
            int anio = hoy.getYear();
            int mes = hoy.getMonthValue();

            List<Object[]> resultados = asistenciaRepository.findTopClientesByMonth(anio, mes);
            List<TopClienteDTO> topClientes = new ArrayList<>();

            int count = 0;
            for (Object[] fila : resultados) {
                if (count >= limite) break;

                Long clienteId = (Long) fila[0];
                Long totalAsistencias = (Long) fila[1];

                // Buscar nombre del cliente
                String nombreCompleto = clienteRepository.findById(clienteId)
                    .map(c -> c.getNombreCompleto())
                    .orElse("Cliente #" + clienteId);

                topClientes.add(new TopClienteDTO(clienteId, nombreCompleto, totalAsistencias.intValue()));
                count++;
            }

            return topClientes;
        } catch (Exception e) {
            log.error("❌ Error al obtener top clientes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Calcula el tiempo relativo (ej: "Hace 5 minutos", "Hace 2 horas").
     * Sobrecarga para LocalDateTime.
     */
    private String calcularTiempoRelativo(LocalDateTime fecha) {
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = java.time.Duration.between(fecha, ahora).toMinutes();

        if (minutos < 1) return "Justo ahora";
        if (minutos < 60) return String.format("Hace %d minuto%s", minutos, minutos == 1 ? "" : "s");
        
        long horas = minutos / 60;
        if (horas < 24) return String.format("Hace %d hora%s", horas, horas == 1 ? "" : "s");
        
        long dias = horas / 24;
        return String.format("Hace %d día%s", dias, dias == 1 ? "" : "s");
    }

    /**
     * Calcula el tiempo relativo para Instant (usado en Pago).
     */
    private String calcularTiempoRelativo(java.time.Instant fecha) {
        java.time.Instant ahora = java.time.Instant.now();
        long minutos = java.time.Duration.between(fecha, ahora).toMinutes();

        if (minutos < 1) return "Justo ahora";
        if (minutos < 60) return String.format("Hace %d minuto%s", minutos, minutos == 1 ? "" : "s");
        
        long horas = minutos / 60;
        if (horas < 24) return String.format("Hace %d hora%s", horas, horas == 1 ? "" : "s");
        
        long dias = horas / 24;
        return String.format("Hace %d día%s", dias, dias == 1 ? "" : "s");
    }
}
