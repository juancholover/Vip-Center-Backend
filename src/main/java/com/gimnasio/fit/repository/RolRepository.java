package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> { // ✅ Cambiado a Long
    Optional<Rol> findByNombre(String nombre);
}