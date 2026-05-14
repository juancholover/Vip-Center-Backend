package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.HistorialPagoFallido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para historial de pagos fallidos (HU-33).
 */
@Repository
public interface HistorialPagoFallidoRepository extends JpaRepository<HistorialPagoFallido, Long> {

    /**
     * Obtiene los pagos fallidos de un cliente, ordenados por fecha descendente.
     */
    List<HistorialPagoFallido> findByClienteIdOrderByFechaRegistroDesc(Long clienteId);

    /**
     * Obtiene todos los pagos fallidos ordenados por fecha descendente.
     */
    List<HistorialPagoFallido> findAllByOrderByFechaRegistroDesc();
}
