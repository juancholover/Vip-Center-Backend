package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.TokenInvalido;
import com.gimnasio.fit.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TokenInvalidoRepository extends JpaRepository<TokenInvalido, Integer> {

    /**
     * Verifica si un token está en la blacklist.
     * @param tokenHash Hash SHA-256 del token
     * @return true si el token está invalidado
     */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Busca un token invalidado por su hash.
     * @param tokenHash Hash del token
     * @return Optional con el registro si existe
     */
    Optional<TokenInvalido> findByTokenHash(String tokenHash);

    /**
     * Elimina todos los tokens expirados (limpieza automática).
     * Se ejecuta mediante scheduled task.
     * @param ahora Timestamp actual
     * @return Número de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM TokenInvalido t WHERE t.fechaExpiracion < :ahora")
    int deleteByFechaExpiracionBefore(@Param("ahora") Instant ahora);

    /**
     * Invalida todos los tokens de un usuario específico.
     * Útil cuando admin necesita cerrar todas las sesiones de un usuario.
     * @param usuario Usuario cuyas sesiones se invalidarán
     * @return Número de tokens invalidados
     */
    @Query("SELECT COUNT(t) FROM TokenInvalido t WHERE t.usuario = :usuario")
    long countByUsuario(@Param("usuario") Usuario usuario);
}
