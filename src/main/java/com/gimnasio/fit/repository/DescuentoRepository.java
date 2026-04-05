package com.gimnasio.fit.repository;

import com.gimnasio.fit.entity.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {
    
    /**
     * Busca descuentos por estado ordenados por porcentaje (menor a mayor)
     */
    List<Descuento> findByEstadoOrderByOrdenAsc(Boolean estado);
    
    /**
     * Busca todos los descuentos ordenados por orden
     */
    List<Descuento> findAllByOrderByOrdenAsc();
    
    /**
     * Busca descuento por nombre
     */
    Optional<Descuento> findByNombre(String nombre);
    
    /**
     * Verifica si existe un nombre (útil para validación de duplicados)
     */
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
