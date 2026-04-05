package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Renovacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RenovacionRepository extends JpaRepository<Renovacion, Long> {

    /**
     * Cuenta renovaciones en un rango de fechas.
     * @param inicio Fecha inicial del rango
     * @param fin Fecha final del rango
     * @return Cantidad de renovaciones
     */
    @Query("SELECT COUNT(r) FROM Renovacion r WHERE r.fechaRenovacion BETWEEN :inicio AND :fin")
    long countRenovacionesEnRango(@Param("inicio") Instant inicio, @Param("fin") Instant fin);

    /**
     * Obtiene renovaciones agrupadas por mes en los últimos 12 meses.
     * @return Lista de objetos con mes y cantidad de renovaciones
     */
    @Query("""
        SELECT FUNCTION('DATE_FORMAT', r.fechaRenovacion, '%Y-%m') as mes,
               COUNT(r) as cantidad
        FROM Renovacion r
        WHERE r.fechaRenovacion >= :inicioRango
        GROUP BY FUNCTION('DATE_FORMAT', r.fechaRenovacion, '%Y-%m')
        ORDER BY mes
    """)
    List<Object[]> contarRenovacionesPorMes(@Param("inicioRango") Instant inicioRango);

    /**
     * Obtiene todas las renovaciones de un cliente.
     */
    @Query("SELECT r FROM Renovacion r WHERE r.cliente.id = :clienteId ORDER BY r.fechaRenovacion DESC")
    List<Renovacion> findByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Obtiene las últimas N renovaciones del sistema.
     */
    @Query("SELECT r FROM Renovacion r ORDER BY r.fechaRenovacion DESC")
    List<Renovacion> findTopRecientes();
}
