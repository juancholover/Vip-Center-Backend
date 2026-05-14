package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.ActualizarSeguimientoDTO;
import com.gimnasio.fit.dto.ClientePorVencerDTO;
import com.gimnasio.fit.entity.Cliente;
import com.gimnasio.fit.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de recepción (HU-32).
 * Proporciona la bandeja de clientes por vencer con prioridad de contacto.
 */
@RestController
@RequestMapping("/api/recepcion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecepcionController {

    private final ClienteRepository clienteRepository;

    /**
     * GET /api/recepcion/por-vencer
     * Obtiene la bandeja de clientes próximos a vencer, ordenados de forma
     * ascendente por días restantes (los de 1 día aparecen primero).
     *
     * @param dias Días de anticipación (default 15)
     */
    @GetMapping("/por-vencer")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<ClientePorVencerDTO>> obtenerClientesPorVencer(
            @RequestParam(defaultValue = "15") Integer dias
    ) {
        log.info("📋 GET /api/recepcion/por-vencer?dias={}", dias);

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);

        // Buscar clientes cuya membresía vence entre hoy y hoy+dias (inclusive)
        List<Object[]> resultados = clienteRepository.findClientesProximosVencer(hoy, limite);

        List<ClientePorVencerDTO> bandeja = new ArrayList<>();

        for (Object[] fila : resultados) {
            Long clienteId = (Long) fila[0];
            String nombre = (String) fila[1];
            String apellido = (String) fila[2];
            LocalDate fechaVencimiento = (LocalDate) fila[3];
            String nombreMembresia = (String) fila[4];

            // Obtener datos adicionales del cliente
            Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
            if (cliente == null) continue;

            String nombreCompleto = nombre + " " + apellido;
            long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVencimiento);
            String avatar = nombre.substring(0, 1).toUpperCase() + apellido.substring(0, 1).toUpperCase();

            // Estado de seguimiento (con fallback a PENDIENTE)
            String estadoSeguimiento = cliente.getEstadoSeguimiento();
            if (estadoSeguimiento == null || estadoSeguimiento.isBlank()) {
                estadoSeguimiento = "PENDIENTE";
            }

            bandeja.add(new ClientePorVencerDTO(
                clienteId,
                nombreCompleto,
                cliente.getTelefono(),
                cliente.getEmail(),
                nombreMembresia,
                fechaVencimiento,
                (int) diasRestantes,
                estadoSeguimiento,
                avatar
            ));
        }

        // Ordenar por días restantes ascendente (1 día primero)
        bandeja.sort(Comparator.comparingInt(ClientePorVencerDTO::getDiasRestantes));

        log.info("✅ Bandeja de recepción: {} clientes por vencer", bandeja.size());
        return ResponseEntity.ok(bandeja);
    }

    /**
     * PATCH /api/recepcion/clientes/{id}/seguimiento
     * Actualiza el estado de seguimiento de un cliente.
     * 
     * @param id ID del cliente
     * @param dto DTO con el nuevo estado: "PENDIENTE", "LLAMADO", "PROMESA"
     */
    @PatchMapping("/clientes/{id}/seguimiento")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Map<String, Object>> actualizarSeguimiento(
            @PathVariable Long id,
            @RequestBody ActualizarSeguimientoDTO dto
    ) {
        log.info("🔄 PATCH /api/recepcion/clientes/{}/seguimiento -> {}", id, dto.getEstadoSeguimiento());

        // Validar estado
        List<String> estadosValidos = List.of("PENDIENTE", "LLAMADO", "PROMESA");
        if (dto.getEstadoSeguimiento() == null || !estadosValidos.contains(dto.getEstadoSeguimiento().toUpperCase())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Estado inválido",
                "mensaje", "Los estados válidos son: PENDIENTE, LLAMADO, PROMESA",
                "estadoRecibido", dto.getEstadoSeguimiento() != null ? dto.getEstadoSeguimiento() : "null"
            ));
        }

        // Buscar cliente
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }

        // Actualizar estado
        String estadoAnterior = cliente.getEstadoSeguimiento();
        cliente.setEstadoSeguimiento(dto.getEstadoSeguimiento().toUpperCase());
        clienteRepository.save(cliente);

        log.info("✅ Estado de seguimiento actualizado: {} -> {} (cliente: {})",
                estadoAnterior, dto.getEstadoSeguimiento(), id);

        return ResponseEntity.ok(Map.of(
            "clienteId", id,
            "estadoAnterior", estadoAnterior != null ? estadoAnterior : "PENDIENTE",
            "estadoNuevo", dto.getEstadoSeguimiento().toUpperCase(),
            "mensaje", "Estado de seguimiento actualizado correctamente"
        ));
    }
}
