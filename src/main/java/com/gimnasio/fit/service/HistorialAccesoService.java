package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.HistorialAccesoDTO;
import com.gimnasio.fit.entity.HistorialAcceso;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface HistorialAccesoService {
    
    /**
     * Registrar un evento en el historial
     */
    HistorialAcceso registrarEvento(
        Long usuarioId,
        String username,
        HistorialAcceso.TipoEvento tipoEvento,
        String ipAddress,
        String userAgent,
        Boolean exitoso,
        String detalles
    );
    
    /**
     * Registrar login exitoso
     */
    void registrarLoginExitoso(Long usuarioId, String username, String ip, String userAgent);
    
    /**
     * Registrar login fallido
     */
    void registrarLoginFallido(String username, String ip, String userAgent, String motivo);
    
    /**
     * Registrar logout
     */
    void registrarLogout(Long usuarioId, String username, String ip, String userAgent);
    
    /**
     * Registrar cambio de contraseña
     */
    void registrarCambioPassword(Long usuarioId, String username, String ip, String userAgent);
    
    /**
     * Obtener historial de un usuario (paginado)
     */
    Page<HistorialAccesoDTO> obtenerHistorialUsuario(Long usuarioId, int page, int size);
    
    /**
     * Obtener historial de un usuario por username
     */
    Page<HistorialAccesoDTO> obtenerHistorialPorUsername(String username, int page, int size);
    
    /**
     * Obtener últimos N accesos de un usuario
     */
    List<HistorialAccesoDTO> obtenerUltimosAccesos(Long usuarioId, int limite);
    
    /**
     * Obtener intentos fallidos recientes
     */
    List<HistorialAccesoDTO> obtenerIntentosfallidosRecientes(int horas);
    
    /**
     * Verificar si un usuario tiene muchos intentos fallidos
     */
    boolean tieneMuchosIntentosFallidos(String username, int maxIntentos, int minutosVentana);
    
    /**
     * Obtener último acceso exitoso de un usuario
     */
    HistorialAccesoDTO obtenerUltimoAccesoExitoso(Long usuarioId);
    
    /**
     * Obtener historial por rango de fechas
     */
    List<HistorialAccesoDTO> obtenerHistorialPorFechas(
        Long usuarioId, 
        LocalDateTime inicio, 
        LocalDateTime fin
    );
}
