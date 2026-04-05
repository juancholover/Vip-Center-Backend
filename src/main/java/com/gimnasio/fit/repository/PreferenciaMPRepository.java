package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.PreferenciaMP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenciaMPRepository extends JpaRepository<PreferenciaMP, Long> {
    
    /**
     * Busca una preferencia por su preferenceId de MercadoPago.
     */
    Optional<PreferenciaMP> findByPreferenceId(String preferenceId);
    
    /**
     * Verifica si existe una preferencia con el preferenceId dado.
     */
    boolean existsByPreferenceId(String preferenceId);
}
