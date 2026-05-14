package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.RegistroNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Repositorio para gestión de registros de notificaciones (HU-31).
 */
@Repository
public interface RegistroNotificacionRepository extends JpaRepository<RegistroNotificacion, Long> {

    /**
     * Verifica si ya se envió una notificación a un cliente para un tipo específico,
     * días antes específicos, y la misma fecha de vencimiento.
     * Esto evita enviar duplicados/spam.
     *
     * @param clienteId                  ID del cliente
     * @param tipo                       Tipo de notificación ("EMAIL" o "SMS")
     * @param diasAntes                  Días antes (30, 15, 7, 1)
     * @param fechaVencimientoReferencia Fecha de vencimiento del cliente
     * @return true si ya se envió
     */
    @Query("SELECT COUNT(r) > 0 FROM RegistroNotificacion r " +
           "WHERE r.cliente.id = :clienteId " +
           "AND r.tipo = :tipo " +
           "AND r.diasAntes = :diasAntes " +
           "AND r.fechaVencimientoReferencia = :fechaVencimiento " +
           "AND r.exitoso = true")
    boolean existeNotificacionEnviada(
        @Param("clienteId") Long clienteId,
        @Param("tipo") String tipo,
        @Param("diasAntes") Integer diasAntes,
        @Param("fechaVencimiento") LocalDate fechaVencimiento
    );
}
