package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByClienteIdOrderByFechaRegistroDesc(Integer clienteId);
    Pago findByMpPreferenceId(String mpPreferenceId);

    /**
     * Suma el monto total de pagos aprobados en un rango de fechas.
     * Usa Instant porque Pago.fechaRegistro es de tipo Instant.
     */
    @Query("SELECT SUM(p.montoFinal) FROM Pago p WHERE p.fechaRegistro BETWEEN :inicio AND :fin AND p.estado = 'aprobado'")
    Double sumMontoByFechaBetween(@Param("inicio") Instant inicio, @Param("fin") Instant fin);

    /**
     * Obtiene los últimos 5 pagos por estado ordenados por fecha descendente.
     */
    List<Pago> findTop5ByEstadoOrderByFechaRegistroDesc(String estado);

    /**
     * Cuenta la cantidad de pagos en un rango de fechas.
     */
    Integer countByFechaRegistroBetween(Instant inicio, Instant fin);

    /**
     * Suma el monto total de pagos por estado en un rango de fechas.
     */
    @Query("SELECT SUM(p.montoFinal) FROM Pago p WHERE p.estado = :estado AND p.fechaRegistro BETWEEN :inicio AND :fin")
    Double sumMontoByEstadoAndFechaBetween(
        @Param("estado") String estado, 
        @Param("inicio") Instant inicio, 
        @Param("fin") Instant fin
    );

    /**
     * Obtiene historial de pagos con información del cliente y membresía.
     * Retorna: [pagoId, fechaRegistro, nombreCliente, apellidoCliente, nombreMembresia, metodoPago, montoFinal, estado]
     */
    @Query("SELECT p.id, p.fechaRegistro, p.cliente.nombre, p.cliente.apellido, " +
           "m.nombre, p.metodoPago, p.montoFinal, p.estado " +
           "FROM Pago p " +
           "LEFT JOIN p.membresia m " +
           "ORDER BY p.fechaRegistro DESC")
    List<Object[]> obtenerHistorialPagos(@Param("limite") Integer limite);

    /**
     * Suma ingresos agrupados por tipo de membresía en un rango de fechas.
     * Retorna: [nombreMembresia, SUM(montoFinal)]
     */
    @Query("SELECT p.cliente.membresiaActual.nombre, SUM(p.montoFinal) " +
           "FROM Pago p " +
           "WHERE p.estado = 'aprobado' " +
           "AND p.fechaRegistro BETWEEN :inicio AND :fin " +
           "GROUP BY p.cliente.membresiaActual.nombre " +
           "ORDER BY SUM(p.montoFinal) DESC")
    List<Object[]> sumMontoByMembresiaAndFechaBetween(
        @Param("inicio") Instant inicio, 
        @Param("fin") Instant fin
    );

    /**
     * Suma ingresos agrupados por método de pago en un rango de fechas.
     * Retorna: [metodoPago, SUM(montoFinal), COUNT(*)]
     */
    @Query("SELECT p.metodoPago, SUM(p.montoFinal), COUNT(p) " +
           "FROM Pago p " +
           "WHERE p.estado = 'aprobado' " +
           "AND p.fechaRegistro BETWEEN :inicio AND :fin " +
           "GROUP BY p.metodoPago " +
           "ORDER BY SUM(p.montoFinal) DESC")
    List<Object[]> sumMontoByMetodoPagoAndFechaBetween(
        @Param("inicio") Instant inicio, 
        @Param("fin") Instant fin
    );

    /**
     * Cuenta pagos realizados en un rango de fechas con estado aprobado.
     */
    @Query("SELECT COUNT(p) FROM Pago p " +
           "WHERE p.estado = 'aprobado' " +
           "AND p.fechaRegistro BETWEEN :inicio AND :fin")
    Integer countPagosRealizadosInRange(
        @Param("inicio") Instant inicio, 
        @Param("fin") Instant fin
    );
}

