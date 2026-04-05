package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de asistencias.
 */
@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    /**
     * Buscar asistencias por rango de fechas con EAGER FETCH de cliente y membresía.
     * Esto evita el problema N+1 y trae todos los datos en una sola query con JOIN.
     */
    @Query("SELECT DISTINCT a FROM Asistencia a " +
           "JOIN FETCH a.cliente c " +
           "LEFT JOIN FETCH c.membresiaActual m " +
           "WHERE a.fechaHora BETWEEN :inicio AND :fin " +
           "ORDER BY a.fechaHora DESC")
    List<Asistencia> findByFechaHoraBetweenOrderByFechaHoraDesc(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    /**
     * Buscar todas las asistencias de un cliente específico con EAGER FETCH.
     */
    @Query("SELECT DISTINCT a FROM Asistencia a " +
           "JOIN FETCH a.cliente c " +
           "LEFT JOIN FETCH c.membresiaActual m " +
           "WHERE c.id = :clienteId " +
           "ORDER BY a.fechaHora DESC")
    List<Asistencia> findByClienteIdOrderByFechaHoraDesc(@Param("clienteId") Long clienteId);

    /**
     * Verificar si el cliente ya registró asistencia hoy
     */
    @Query("SELECT a FROM Asistencia a WHERE a.cliente.id = :clienteId " +
           "AND DATE(a.fechaHora) = CURRENT_DATE")
    Optional<Asistencia> findAsistenciaHoyByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Versión robusta: busca la última asistencia del día usando rangos explícitos
     * evitando dependencias del dialecto (DATE(), CURRENT_DATE).
     */
    Optional<Asistencia> findFirstByClienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(
           Long clienteId,
           LocalDateTime inicio,
           LocalDateTime fin
    );

    /**
     * Contar asistencias de un cliente en un mes específico
     */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.cliente.id = :clienteId " +
           "AND YEAR(a.fechaHora) = :anio AND MONTH(a.fechaHora) = :mes")
    Long countAsistenciasByClienteAndMonth(
            @Param("clienteId") Long clienteId,
            @Param("anio") int anio,
            @Param("mes") int mes
    );

    /**
     * Obtener los 10 clientes más frecuentes del mes
     */
    @Query("SELECT a.cliente.id, COUNT(a) as total FROM Asistencia a " +
           "WHERE YEAR(a.fechaHora) = :anio AND MONTH(a.fechaHora) = :mes " +
           "GROUP BY a.cliente.id ORDER BY total DESC")
    List<Object[]> findTopClientesByMonth(@Param("anio") int anio, @Param("mes") int mes);

       /**
        * Obtiene la fecha-hora de la última asistencia del cliente (o NULL si no tiene).
        */
       @Query("SELECT MAX(a.fechaHora) FROM Asistencia a WHERE a.cliente.id = :clienteId")
       LocalDateTime findUltimaAsistenciaByClienteId(@Param("clienteId") Long clienteId);

              /**
               * Borra todas las asistencias de un cliente en el rango dado (inclusive).
               */
              @Modifying
              @Query("DELETE FROM Asistencia a WHERE a.cliente.id = :clienteId AND a.fechaHora BETWEEN :inicio AND :fin")
              int deleteByClienteIdAndFechaHoraBetween(@Param("clienteId") Long clienteId,
                                                                                     @Param("inicio") LocalDateTime inicio,
                                                                                     @Param("fin") LocalDateTime fin);

    /**
     * Cuenta asistencias en un rango de fechas.
     */
    Integer countByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene asistencias en un rango de fechas (sin FETCH para agregación).
     */
    List<Asistencia> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene las últimas 5 asistencias ordenadas por fecha descendente.
     */
    List<Asistencia> findTop5ByOrderByFechaHoraDesc();

    /**
     * Obtiene asistencias agrupadas por cliente con totales y estadísticas.
     * Retorna: [clienteId, nombre, apellido, email, telefono, COUNT, MIN(fechaHora), MAX(fechaHora), estado, fechaVencimiento]
     */
    @Query("SELECT " +
           "a.cliente.id, " +
           "a.cliente.nombre, " +
           "a.cliente.apellido, " +
           "a.cliente.email, " +
           "a.cliente.telefono, " +
           "COUNT(a), " +
           "MIN(a.fechaHora), " +
           "MAX(a.fechaHora), " +
           "CASE WHEN a.cliente.fechaVencimiento >= CURRENT_DATE THEN 'activo' ELSE 'vencido' END, " +
           "a.cliente.fechaVencimiento " +
           "FROM Asistencia a " +
           "WHERE a.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY a.cliente.id, a.cliente.nombre, a.cliente.apellido, a.cliente.email, a.cliente.telefono, a.cliente.fechaVencimiento " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> obtenerAsistenciasPorCliente(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fin") LocalDateTime fin
    );

    /**
     * Obtiene asistencias agrupadas por hora del día.
     * Retorna: [hora, COUNT]
     */
    @Query("SELECT HOUR(a.fechaHora), COUNT(a) " +
           "FROM Asistencia a " +
           "WHERE a.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY HOUR(a.fechaHora) " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> obtenerAsistenciasPorHora(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fin") LocalDateTime fin
    );

    /**
     * Obtiene clientes ausentes (sin asistencias desde una fecha límite).
     * SOLO clientes con membresía activa y QR activo.
     * Retorna: [clienteId, nombre, apellido, MAX(fechaHora), estado]
     * 
     * 🔥 NOTA: Usamos COALESCE para manejar NULL en HQL/JPQL
     * Si MAX(a.fechaHora) es NULL (nunca ha asistido), usa '2000-01-01'
     * para que SIEMPRE sea menor que fechaLimite
     */
    @Query("SELECT " +
           "c.id, " +
           "c.nombre, " +
           "c.apellido, " +
           "MAX(a.fechaHora), " +
           "CASE WHEN c.fechaVencimiento >= CURRENT_DATE THEN 'activo' ELSE 'vencido' END " +
           "FROM Cliente c " +
           "INNER JOIN c.membresiaActual m " +
           "LEFT JOIN Asistencia a ON a.cliente.id = c.id " +
           "WHERE c.qrActivo = true " +
           "GROUP BY c.id, c.nombre, c.apellido, c.fechaVencimiento " +
           "HAVING COALESCE(MAX(a.fechaHora), CAST('2000-01-01' AS timestamp)) < :fechaLimite " +
           "ORDER BY COALESCE(MAX(a.fechaHora), CAST('2000-01-01' AS timestamp)) ASC")
    List<Object[]> obtenerClientesAusentes(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Obtiene las últimas N asistencias con información del cliente y membresía.
     * Retorna: [asistenciaId, nombre, apellido, fechaHora, nombreMembresia, estado]
     */
    @Query("SELECT " +
           "a.id, " +
           "a.cliente.nombre, " +
           "a.cliente.apellido, " +
           "a.fechaHora, " +
           "a.cliente.membresiaActual.nombre, " +
           "CASE WHEN a.cliente.fechaVencimiento >= CURRENT_DATE THEN 'Activo' ELSE 'Vencido' END " +
           "FROM Asistencia a " +
           "ORDER BY a.fechaHora DESC")
    List<Object[]> obtenerAsistenciasRecientes(@Param("limite") Integer limite);
}

