package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.dto.DescuentoDTO;
import com.gimnasio.fit.entity.Descuento;
import com.gimnasio.fit.repository.DescuentoRepository;
import com.gimnasio.fit.service.DescuentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DescuentoServiceImpl implements DescuentoService {

    private final DescuentoRepository descuentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DescuentoDTO> listarTodos() {
        return descuentoRepository.findAllByOrderByOrdenAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DescuentoDTO> listarActivos() {
        return descuentoRepository.findByEstadoOrderByOrdenAsc(true).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DescuentoDTO obtenerPorId(Long id) {
        Descuento descuento = descuentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Descuento no encontrado con ID: " + id));
        return toDTO(descuento);
    }

    @Override
    @Transactional
    public DescuentoDTO crear(DescuentoDTO dto) {
        // Validar nombre único
        descuentoRepository.findByNombre(dto.getNombre()).ifPresent(d -> {
            throw new RuntimeException("Ya existe un descuento con el nombre: " + dto.getNombre());
        });

        Descuento descuento = new Descuento();
        descuento.setNombre(dto.getNombre());
        descuento.setPorcentaje(dto.getPorcentaje());
        descuento.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        
        // Guardar primero sin orden
        descuento = descuentoRepository.save(descuento);
        
        // Recalcular orden de todos
        recalcularOrden();
        
        return toDTO(descuentoRepository.findById(descuento.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public DescuentoDTO actualizar(Long id, DescuentoDTO dto) {
        Descuento descuento = descuentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Descuento no encontrado"));

        // Validar nombre único (excepto el actual)
        if (!descuento.getNombre().equals(dto.getNombre())) {
            if (descuentoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
                throw new RuntimeException("Ya existe un descuento con el nombre: " + dto.getNombre());
            }
        }

        boolean cambioPorcentaje = !descuento.getPorcentaje().equals(dto.getPorcentaje());

        descuento.setNombre(dto.getNombre());
        descuento.setPorcentaje(dto.getPorcentaje());
        descuento.setEstado(dto.getEstado());
        
        descuento = descuentoRepository.save(descuento);
        
        // Si cambió el porcentaje, recalcular orden
        if (cambioPorcentaje) {
            recalcularOrden();
        }
        
        return toDTO(descuentoRepository.findById(descuento.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public DescuentoDTO cambiarEstado(Long id, Boolean nuevoEstado) {
        Descuento descuento = descuentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Descuento no encontrado"));
        
        descuento.setEstado(nuevoEstado);
        descuento = descuentoRepository.save(descuento);
        
        log.info("Estado de descuento {} cambiado a: {}", descuento.getNombre(), nuevoEstado);
        return toDTO(descuento);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!descuentoRepository.existsById(id)) {
            throw new RuntimeException("Descuento no encontrado");
        }
        descuentoRepository.deleteById(id);
        log.info("Descuento eliminado con ID: {}", id);
        
        // Recalcular orden después de eliminar
        recalcularOrden();
    }

    @Override
    @Transactional
    public void recalcularOrden() {
        List<Descuento> descuentos = descuentoRepository.findAll();
        
        // Ordenar por porcentaje (menor a mayor)
        descuentos.sort(Comparator.comparing(Descuento::getPorcentaje));
        
        // Asignar nuevo orden
        for (int i = 0; i < descuentos.size(); i++) {
            descuentos.get(i).setOrden(i);
        }
        
        descuentoRepository.saveAll(descuentos);
        log.info("Orden de descuentos recalculado: {} elementos", descuentos.size());
    }

    private DescuentoDTO toDTO(Descuento descuento) {
        DescuentoDTO dto = new DescuentoDTO();
        dto.setId(descuento.getId());
        dto.setNombre(descuento.getNombre());
        dto.setPorcentaje(descuento.getPorcentaje());
        dto.setOrden(descuento.getOrden());
        dto.setEstado(descuento.getEstado());
        return dto;
    }
}
