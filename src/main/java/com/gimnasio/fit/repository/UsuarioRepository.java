package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> { // ✅ Cambiado a Long

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    // Carga roles y permisos para autenticación
    @Query("""
            SELECT u FROM Usuario u
            LEFT JOIN FETCH u.usuarioRoles ur
            LEFT JOIN FETCH ur.rol r
            LEFT JOIN FETCH r.rolPermisos rp
            LEFT JOIN FETCH rp.permiso p
            WHERE u.email = :email
            """)
    Optional<Usuario> fetchWithRolesAndPermisos(String email);
    
    // Carga todos los usuarios con sus roles (para listado)
    @Query("""
            SELECT DISTINCT u FROM Usuario u
            LEFT JOIN FETCH u.usuarioRoles ur
            LEFT JOIN FETCH ur.rol r
            ORDER BY u.id
            """)
    List<Usuario> findAllWithRoles();
}