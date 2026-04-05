package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Integer> {
    
    @Modifying
    @Query("DELETE FROM UsuarioRol ur WHERE ur.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}