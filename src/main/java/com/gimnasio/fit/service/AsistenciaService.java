package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.AsistenciaDTO;
import com.gimnasio.fit.dto.RegistrarAsistenciaResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz del servicio de asistencias.
 */
public interface AsistenciaService {

    /**
     * Registrar asistencia por QR (público).
     * @param token Token QR del cliente
     * @param dispositivo User-Agent del dispositivo
     * @param ipAddress IP del cliente
     * @param latitud Coordenada GPS (opcional)
     * @param longitud Coordenada GPS (opcional)
     * @return Respuesta con información de la asistencia registrada
     */
    RegistrarAsistenciaResponse registrarPorToken(
            String token,
            String dispositivo,
            String ipAddress,
            Double latitud,
            Double longitud
    );

    /**
     * Registrar asistencia manualmente por el staff.
     * @param clienteId ID del cliente
     * @param notas Notas adicionales
     * @param dispositivo User-Agent del dispositivo
     * @param ipAddress IP del staff
     * @return Respuesta con información de la asistencia registrada
     */
    RegistrarAsistenciaResponse registrarManual(
            Long clienteId,
            String notas,
            String dispositivo,
            String ipAddress
    );

    /**
     * Obtener asistencias de hoy.
     * @return Lista de asistencias del día
     */
    List<AsistenciaDTO> obtenerAsistenciasHoy();

    /**
     * Obtener asistencias por rango de fechas.
     * @param inicio Fecha de inicio
     * @param fin Fecha de fin
     * @return Lista de asistencias en el rango
     */
    List<AsistenciaDTO> obtenerAsistenciasPorRango(LocalDate inicio, LocalDate fin);

    /**
     * Obtener historial de asistencias de un cliente.
     * @param clienteId ID del cliente
     * @return Lista de asistencias del cliente
     */
    List<AsistenciaDTO> obtenerHistorialCliente(Long clienteId);

    /**
     * Verificar si un cliente ya registró asistencia hoy.
     * @param clienteId ID del cliente
     * @return true si ya registró, false si no
     */
    boolean verificarAsistenciaHoy(Long clienteId);

    /**
     * Contar asistencias de un cliente en un mes específico.
     * @param clienteId ID del cliente
     * @param anio Año
     * @param mes Mes (1-12)
     * @return Cantidad de asistencias
     */
    Long contarAsistenciasMes(Long clienteId, int anio, int mes);

    /**
     * Eliminar una asistencia por su ID.
     * @param id ID de la asistencia a eliminar
     * @throws EntityNotFoundException si la asistencia no existe
     */
    void eliminarAsistencia(Long id);

        /**
         * Elimina todas las asistencias del día actual para un cliente.
         * @param clienteId ID del cliente
         * @return número de asistencias eliminadas
         */
        int eliminarAsistenciasDeHoy(Long clienteId);
}
