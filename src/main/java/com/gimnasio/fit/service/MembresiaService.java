package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.CrearMembresiaRequest;
import com.gimnasio.fit.dto.MembresiaDTO;

import java.util.List;

/**
 * Interfaz del servicio de membresías.
 */
public interface MembresiaService {

    /**
     * Listar todas las membresías activas.
     */
    List<MembresiaDTO> listarActivas();

    /**
     * Listar todas las membresías (activas e inactivas).
     */
    List<MembresiaDTO> listarTodas();

    /**
     * Obtener membresía por ID.
     */
    MembresiaDTO obtenerPorId(Long id);

    /**
     * Obtener membresía por código.
     */
    MembresiaDTO obtenerPorCodigo(String codigo);

    /**
     * Crear nueva membresía.
     */
    MembresiaDTO crear(CrearMembresiaRequest request);

    /**
     * Actualizar membresía existente.
     */
    MembresiaDTO actualizar(Long id, CrearMembresiaRequest request);

    /**
     * Activar/desactivar membresía.
     */
    MembresiaDTO cambiarEstado(Long id, boolean estado);

    /**
     * Eliminar membresía (soft delete - solo desactivar).
     */
    void eliminar(Long id);

    /**
     * Recalcular orden automático de todas las membresías basado en duración.
     */
    void recalcularOrden();
}
