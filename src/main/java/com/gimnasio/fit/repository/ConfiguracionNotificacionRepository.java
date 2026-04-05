package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.ConfiguracionNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionNotificacionRepository extends JpaRepository<ConfiguracionNotificacion, Long> {

    /**
     * Obtiene la primera (y única) configuración.
     * Solo debe existir 1 registro en la tabla.
     */
    Optional<ConfiguracionNotificacion> findFirstByOrderByIdAsc();
}
