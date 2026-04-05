package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.dto.CrearMembresiaRequest;
import com.gimnasio.fit.dto.MembresiaDTO;
import com.gimnasio.fit.entity.Membresia;
import com.gimnasio.fit.entity.Usuario;
import com.gimnasio.fit.repository.MembresiaRepository;
import com.gimnasio.fit.repository.UsuarioRepository;
import com.gimnasio.fit.service.MembresiaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de membresías.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembresiaServiceImpl implements MembresiaService {

    private final MembresiaRepository membresiaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MembresiaDTO> listarActivas() {
        log.info("Listando membresías activas");
        return membresiaRepository.findByEstadoTrueOrderByOrdenAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembresiaDTO> listarTodas() {
        log.info("Listando todas las membresías");
        return membresiaRepository.findAllByOrderByOrdenAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MembresiaDTO obtenerPorId(Long id) {
        log.info("Buscando membresía con ID: {}", id);
        Membresia membresia = membresiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membresía no encontrada con ID: " + id));
        return toDTO(membresia);
    }

    @Override
    @Transactional(readOnly = true)
    public MembresiaDTO obtenerPorCodigo(String codigo) {
        // Método deprecado - mantener por compatibilidad
        log.warn("Método obtenerPorCodigo deprecado - usar obtenerPorId");
        throw new UnsupportedOperationException("Método deprecado - la columna 'codigo' fue eliminada");
    }

    @Override
    @Transactional
    public MembresiaDTO crear(CrearMembresiaRequest request) {
        log.info("Creando nueva membresía: {}", request.getNombre());

        // Validar nombre único
        if (membresiaRepository.existsByNombreAndIdNot(request.getNombre(), 0L)) {
            throw new IllegalArgumentException("Ya existe una membresía con el nombre: " + request.getNombre());
        }

        // Obtener usuario autenticado para auditoría
        Usuario usuarioActual = obtenerUsuarioAutenticado();

        Membresia membresia = Membresia.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .duracionDias(request.getDuracionDias())
                .precio(request.getPrecio())
                .estado(request.getEstado() != null ? request.getEstado() : true)
                .color(request.getColor())
                .orden(0) // Se recalculará automáticamente
                .creadoPor(usuarioActual) // ✅ FIX: Asignar usuario que crea la membresía
                .build();

        Membresia guardada = membresiaRepository.save(membresia);
        
        // Recalcular orden automáticamente
        recalcularOrden();
        
        log.info("✅ Membresía creada exitosamente: {} (ID: {})", guardada.getNombre(), guardada.getId());
        
        return toDTO(membresiaRepository.findById(guardada.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public MembresiaDTO actualizar(Long id, CrearMembresiaRequest request) {
        log.info("Actualizando membresía ID: {}", id);

        Membresia membresia = membresiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membresía no encontrada con ID: " + id));

        // Validar nombre duplicado (excepto el actual)
        if (!membresia.getNombre().equals(request.getNombre()) && 
            membresiaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new IllegalArgumentException("Ya existe una membresía con el nombre: " + request.getNombre());
        }

        boolean cambioDuracion = !membresia.getDuracionDias().equals(request.getDuracionDias());

        membresia.setNombre(request.getNombre());
        membresia.setDescripcion(request.getDescripcion());
        membresia.setDuracionDias(request.getDuracionDias());
        membresia.setPrecio(request.getPrecio());
        membresia.setEstado(request.getEstado() != null ? request.getEstado() : true);
        membresia.setColor(request.getColor());

        Membresia actualizada = membresiaRepository.save(membresia);
        
        // Si cambió la duración, recalcular orden
        if (cambioDuracion) {
            recalcularOrden();
        }
        
        log.info("✅ Membresía actualizada: {}", actualizada.getNombre());
        
        return toDTO(membresiaRepository.findById(actualizada.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public MembresiaDTO cambiarEstado(Long id, boolean estado) {
        log.info("Cambiando estado de membresía ID {} a: {}", id, estado ? "ACTIVA" : "INACTIVA");

        Membresia membresia = membresiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membresía no encontrada con ID: " + id));

        membresia.setEstado(estado);
        Membresia actualizada = membresiaRepository.save(membresia);
        
        log.info("✅ Estado actualizado para: {}", actualizada.getNombre());
        return toDTO(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando membresía ID: {}", id);

        if (!membresiaRepository.existsById(id)) {
            throw new EntityNotFoundException("Membresía no encontrada con ID: " + id);
        }

        membresiaRepository.deleteById(id);
        
        // Recalcular orden después de eliminar
        recalcularOrden();
        
        log.info("✅ Membresía eliminada con ID: {}", id);
    }

    /**
     * Recalcula el orden de todas las membresías según su duración (menor a mayor)
     */
    @Transactional
    public void recalcularOrden() {
        List<Membresia> membresias = membresiaRepository.findAll();
        
        // Ordenar por duración (menor a mayor)
        membresias.sort(Comparator.comparing(Membresia::getDuracionDias));
        
        // Asignar nuevo orden
        for (int i = 0; i < membresias.size(); i++) {
            membresias.get(i).setOrden(i);
        }
        
        membresiaRepository.saveAll(membresias);
        log.info("Orden de membresías recalculado: {} elementos", membresias.size());
    }

    // ========== Métodos auxiliares ==========

    /**
     * Obtiene el usuario autenticado del contexto de seguridad para auditoría
     */
    private Usuario obtenerUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("⚠️ No hay usuario autenticado en el contexto de seguridad");
                return null;
            }
            
            String email = authentication.getName();
            return usuarioRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.warn("⚠️ Usuario con email {} no encontrado en la base de datos", email);
                        return null;
                    });
        } catch (Exception e) {
            log.error("❌ Error al obtener usuario autenticado: {}", e.getMessage());
            return null;
        }
    }

    private MembresiaDTO toDTO(Membresia membresia) {
        return MembresiaDTO.builder()
                .id(membresia.getId())
                .nombre(membresia.getNombre())
                .descripcion(membresia.getDescripcion())
                .duracionDias(membresia.getDuracionDias())
                .precio(membresia.getPrecio())
                .estado(membresia.getEstado())
                .color(membresia.getColor())
                .orden(membresia.getOrden())
                .build();
    }
}
