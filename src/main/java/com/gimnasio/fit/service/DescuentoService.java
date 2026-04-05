package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.DescuentoDTO;

import java.util.List;

public interface DescuentoService {
    
    /**
     * Obtener todos los descuentos ordenados por porcentaje (menor a mayor)
     */
    List<DescuentoDTO> listarTodos();
    
    /**
     * Obtener descuentos activos ordenados
     */
    List<DescuentoDTO> listarActivos();
    
    /**
     * Obtener descuento por ID
     */
    DescuentoDTO obtenerPorId(Long id);
    
    /**
     * Crear nuevo descuento (recalcula orden automáticamente)
     */
    DescuentoDTO crear(DescuentoDTO dto);
    
    /**
     * Actualizar descuento (recalcula orden si cambió el porcentaje)
     */
    DescuentoDTO actualizar(Long id, DescuentoDTO dto);
    
    /**
     * Cambiar estado de descuento
     */
    DescuentoDTO cambiarEstado(Long id, Boolean nuevoEstado);
    
    /**
     * Eliminar descuento
     */
    void eliminar(Long id);
    
    /**
     * Recalcula el orden de todos los descuentos según su porcentaje
     */
    void recalcularOrden();
}
