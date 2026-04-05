package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.HistorialAcceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialAccesoRepository extends JpaRepository<HistorialAcceso, Long> {
    
    // Buscar por usuario
    Page<HistorialAcceso> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId, Pageable pageable);
    
    List<HistorialAcceso> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
    
    // Buscar por username
    Page<HistorialAcceso> findByUsernameOrderByFechaHoraDesc(String username, Pageable pageable);
    
    // Buscar por tipo de evento
    Page<HistorialAcceso> findByTipoEventoOrderByFechaHoraDesc(
        HistorialAcceso.TipoEvento tipoEvento, 
        Pageable pageable
    );
    
    // Buscar intentos fallidos
    @Query("SELECT h FROM HistorialAcceso h WHERE h.exitoso = false " +
           "AND h.fechaHora >= :desde ORDER BY h.fechaHora DESC")
    List<HistorialAcceso> findIntentosFallidos(@Param("desde") LocalDateTime desde);
    
    // Buscar intentos fallidos por usuario
    @Query("SELECT h FROM HistorialAcceso h WHERE h.username = :username " +
           "AND h.exitoso = false AND h.fechaHora >= :desde " +
           "ORDER BY h.fechaHora DESC")
    List<HistorialAcceso> findIntentosfallidosPorUsuario(
        @Param("username") String username, 
        @Param("desde") LocalDateTime desde
    );
    
    // Contar intentos fallidos recientes por username
    @Query("SELECT COUNT(h) FROM HistorialAcceso h WHERE h.username = :username " +
           "AND h.exitoso = false AND h.fechaHora >= :desde")
    long countIntentosfallidosRecientes(
        @Param("username") String username, 
        @Param("desde") LocalDateTime desde
    );
    
    // Último acceso exitoso de un usuario
    @Query("SELECT h FROM HistorialAcceso h WHERE h.usuarioId = :usuarioId " +
           "AND h.tipoEvento = 'LOGIN' AND h.exitoso = true " +
           "ORDER BY h.fechaHora DESC")
    List<HistorialAcceso> findUltimoAccesoExitoso(@Param("usuarioId") Long usuarioId);
    
    // Buscar por rango de fechas
    @Query("SELECT h FROM HistorialAcceso h WHERE h.usuarioId = :usuarioId " +
           "AND h.fechaHora BETWEEN :inicio AND :fin " +
           "ORDER BY h.fechaHora DESC")
    List<HistorialAcceso> findByUsuarioAndFechaRange(
        @Param("usuarioId") Long usuarioId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );
    
    // Estadísticas de accesos por día
    @Query("SELECT DATE(h.fechaHora) as fecha, COUNT(h) as total " +
           "FROM HistorialAcceso h WHERE h.usuarioId = :usuarioId " +
           "AND h.fechaHora >= :desde " +
           "GROUP BY DATE(h.fechaHora) ORDER BY fecha DESC")
    List<Object[]> countAccesosPorDia(
        @Param("usuarioId") Long usuarioId, 
        @Param("desde") LocalDateTime desde
    );
}
