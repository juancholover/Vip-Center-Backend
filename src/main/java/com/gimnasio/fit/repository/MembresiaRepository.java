package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestión de membresías.
 */
@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {

    /**
     * Listar solo membresías activas, ordenadas por duración (menor a mayor).
     */
    List<Membresia> findByEstadoTrueOrderByOrdenAsc();

    /**
     * Listar todas las membresías ordenadas por orden.
     */
    List<Membresia> findAllByOrderByOrdenAsc();

    /**
     * Verificar si existe una membresía con el nombre dado (excepto la actual).
     */
    boolean existsByNombreAndIdNot(String nombre, Long id);

    /**
     * Buscar membresías por rango de precio.
     */
    @Query("SELECT m FROM Membresia m WHERE m.precio BETWEEN :precioMin AND :precioMax " +
           "AND m.estado = true ORDER BY m.precio ASC")
    List<Membresia> findByPrecioRango(java.math.BigDecimal precioMin, java.math.BigDecimal precioMax);

    /**
     * Buscar membresías por duración en días.
     */
    List<Membresia> findByDuracionDiasAndEstadoTrue(Integer duracionDias);

    /**
     * Obtiene reporte de membresías por ventas e ingresos en un período.
     * Retorna: [membresiaId, nombre, precio, duracionDias, cantidadVentas, totalIngresos, clientesActivos, clientesVencidos]
     */
    @Query("SELECT " +
           "m.id, " +
           "m.nombre, " +
           "m.precio, " +
           "m.duracionDias, " +
           "COUNT(DISTINCT p.id), " +
           "SUM(p.montoFinal), " +
           "COUNT(DISTINCT CASE WHEN c.fechaVencimiento >= CURRENT_DATE THEN c.id END), " +
           "COUNT(DISTINCT CASE WHEN c.fechaVencimiento < CURRENT_DATE THEN c.id END) " +
           "FROM Membresia m " +
           "LEFT JOIN Pago p ON p.membresia.id = m.id " +
           "LEFT JOIN Cliente c ON c.membresiaActual.id = m.id " +
           "WHERE p.fechaRegistro BETWEEN :inicio AND :fin " +
           "AND p.estado = 'approved' " +
           "GROUP BY m.id, m.nombre, m.precio, m.duracionDias " +
           "ORDER BY COUNT(DISTINCT p.id) DESC")
    List<Object[]> obtenerReporteMembresiasPorVentas(
        @Param("inicio") java.time.Instant inicio, 
        @Param("fin") java.time.Instant fin
    );
}
