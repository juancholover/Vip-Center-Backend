package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Long> { // ✅ Cambiado a Long
    Optional<Permiso> findByCodigo(String codigo);
    boolean existsByModuloAndAccion(String modulo, String accion);
}