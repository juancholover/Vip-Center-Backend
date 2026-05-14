package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    /**
     * Busca cliente por teléfono (útil para registro/búsqueda rápida).
     */
    Optional<Cliente> findByTelefono(String telefono);

    /**
     * Busca cliente por código QR (para validación de acceso).
     */
    Optional<Cliente> findByQrAcceso(String qrAcceso);

        /**
         * Versión con bloqueo pesimista para flujos de registro de asistencia por QR.
         * Evita condiciones de carrera cuando llegan dos peticiones simultáneas.
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT c FROM Cliente c WHERE c.qrAcceso = :qr")
        Optional<Cliente> findByQrAccesoForUpdate(@Param("qr") String qrAcceso);

    /**
     * Busca cliente por email.
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Verifica si ya existe un cliente con la combinación nombre+apellido+telefono.
     * (Para validar el constraint único antes de guardar)
     */
    boolean existsByNombreAndApellidoAndTelefono(String nombre, String apellido, String telefono);

    /**
     * Busca clientes por nombre o apellido (búsqueda parcial).
     */
    @Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))
            OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :termino, '%'))
            ORDER BY c.nombre, c.apellido
            """)
    List<Cliente> buscarPorNombreOApellido(@Param("termino") String termino);

    /**
     * Lista clientes con membresía activa (fecha_vencimiento >= HOY y qr_activo = true).
     */
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.fechaVencimiento >= :hoy
            AND c.qrActivo = true
            ORDER BY c.fechaVencimiento ASC
            """)
    List<Cliente> listarActivos(@Param("hoy") LocalDate hoy);

    /**
     * Lista clientes con membresía vencida.
     */
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.fechaVencimiento < :hoy
            ORDER BY c.fechaVencimiento DESC
            """)
    List<Cliente> listarVencidos(@Param("hoy") LocalDate hoy);

    /**
     * Lista clientes sin membresía (nunca han pagado).
     */
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.fechaVencimiento IS NULL
            ORDER BY c.fechaRegistro DESC
            """)
    List<Cliente> listarSinMembresia();

    /**
     * Lista clientes que vencen en los próximos N días (para alertas).
     */
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.fechaVencimiento BETWEEN :hoy AND :fechaLimite
            AND c.qrActivo = true
            ORDER BY c.fechaVencimiento ASC
            """)
    List<Cliente> listarProximosAVencer(@Param("hoy") LocalDate hoy, 
                                        @Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Cuenta clientes con membresía activa que vencen en los próximos N días.
     * Simplificado: solo busca en tabla clientes con fechaVencimiento.
     */
    @Query("""
            SELECT COUNT(c) FROM Cliente c 
            WHERE c.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin 
            AND c.membresiaActual IS NOT NULL
            AND c.qrActivo = true
            """)
    long countProximosAVencerEnRango(@Param("fechaInicio") LocalDate fechaInicio, 
                                      @Param("fechaFin") LocalDate fechaFin);

    /**
     * Lista todos los clientes con carga eager de la relación registradoPor.
     */
    @Query("""
            SELECT DISTINCT c FROM Cliente c
            LEFT JOIN FETCH c.registradoPor
            ORDER BY c.id
            """)
    List<Cliente> findAllWithRegistrador();

    /**
     * Cuenta clientes activos: con membresía válida (fecha >= hoy) y QR activo.
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.fechaVencimiento >= :hoy AND c.qrActivo = true")
    Integer countClientesActivos(@Param("hoy") LocalDate hoy);

    /**
     * Cuenta clientes cuya membresía vence en un rango de fechas.
     */
    Integer countByFechaVencimientoBetween(LocalDate inicio, LocalDate fin);

    /**
     * Cuenta clientes con membresía vencida.
     */
    Integer countByFechaVencimientoBefore(LocalDate fecha);

    /**
     * Cuenta clientes con membresía activa (no vencida).
     */
    Integer countByFechaVencimientoAfter(LocalDate fecha);

    /**
     * Obtiene clientes agrupados por tipo de membresía.
     * Retorna: [nombreMembresia, COUNT]
     */
    @Query("SELECT c.membresiaActual.nombre, COUNT(c) " +
           "FROM Cliente c " +
           "WHERE c.membresiaActual IS NOT NULL " +
           "GROUP BY c.membresiaActual.nombre " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> countByMembresia();

    /**
     * Obtiene clientes próximos a vencer en un rango de fechas.
     * SOLO clientes con membresía activa y QR activo.
     * Retorna: [clienteId, nombre, apellido, fechaVencimiento, nombreMembresia]
     */
    @Query("SELECT c.id, c.nombre, c.apellido, c.fechaVencimiento, " +
           "m.nombre " +
           "FROM Cliente c " +
           "INNER JOIN c.membresiaActual m " +
           "WHERE c.fechaVencimiento BETWEEN :inicio AND :fin " +
           "AND c.qrActivo = true " +
           "ORDER BY c.fechaVencimiento ASC")
    List<Object[]> findClientesProximosVencer(
        @Param("inicio") LocalDate inicio, 
        @Param("fin") LocalDate fin
    );

    /**
     * Cuenta nuevos clientes registrados en un rango de fechas.
     */
    @Query("SELECT COUNT(c) FROM Cliente c " +
           "WHERE c.fechaRegistro BETWEEN :inicio AND :fin")
    Integer countNuevosClientesInRange(
        @Param("inicio") LocalDate inicio, 
        @Param("fin") LocalDate fin
    );

    /**
     * Cuenta renovaciones (clientes que renovaron) en un rango de fechas.
     * Una renovación es cuando un cliente EXISTENTE (no nuevo) realiza un pago.
     * Se identifica porque el cliente fue registrado ANTES del inicio del periodo.
     */
    @Query("SELECT COUNT(DISTINCT p.id) FROM Pago p " +
           "WHERE p.estado = 'approved' " +
           "AND p.fechaRegistro BETWEEN :inicio AND :fin " +
           "AND p.cliente.fechaRegistro < :inicioPeriodo")
    Integer countRenovacionesInRange(
        @Param("inicio") java.time.Instant inicio, 
        @Param("fin") java.time.Instant fin,
        @Param("inicioPeriodo") java.time.Instant inicioPeriodo
    );

    /**
     * Calcula tasa de retención: clientes activos que NO son nuevos / total clientes no nuevos.
     */
    @Query("SELECT COUNT(c) FROM Cliente c " +
           "WHERE c.fechaVencimiento >= :hoy " +
           "AND c.qrActivo = true " +
           "AND c.fechaRegistro < :inicioMes")
    Integer countClientesRetenidos(
        @Param("hoy") LocalDate hoy,
        @Param("inicioMes") LocalDate inicioMes
    );

    @Query("SELECT COUNT(c) FROM Cliente c " +
           "WHERE c.fechaRegistro < :inicioMes")
    Integer countClientesAntiguos(@Param("inicioMes") LocalDate inicioMes);

    /**
     * Cuenta clientes que cancelaron (QR desactivado) en un mes específico.
     * Nota: Esta es una aproximación, idealmente deberías tener un campo fecha_cancelacion.
     */
    @Query("SELECT COUNT(c) FROM Cliente c " +
           "WHERE c.qrActivo = false " +
           "AND c.fechaVencimiento BETWEEN :inicioMes AND :finMes")
    Integer countCancelacionesInRange(
        @Param("inicioMes") LocalDate inicioMes,
        @Param("finMes") LocalDate finMes
    );

    /**
     * Busca clientes cuya membresía vence en una fecha específica (HU-31).
     * Usado por el scheduler de recordatorios de vencimiento.
     * Solo retorna clientes con QR activo (membresía vigente).
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.fechaVencimiento = :fecha " +
           "AND c.qrActivo = true")
    List<Cliente> findByFechaVencimiento(@Param("fecha") LocalDate fecha);
}

