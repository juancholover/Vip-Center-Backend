package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.dto.AsistenciaDTO;
import com.gimnasio.fit.dto.RegistrarAsistenciaResponse;
import com.gimnasio.fit.entity.Asistencia;
import com.gimnasio.fit.entity.Cliente;
import com.gimnasio.fit.entity.TipoRegistro;
import com.gimnasio.fit.repository.AsistenciaRepository;
import com.gimnasio.fit.repository.ClienteRepository;
import com.gimnasio.fit.service.AsistenciaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de asistencias.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public RegistrarAsistenciaResponse registrarPorToken(
            String token,
            String dispositivo,
            String ipAddress,
            Double latitud,
            Double longitud
    ) {
        log.info("Intentando registrar asistencia con token: {}", token);

        // 1. Buscar cliente por QR
                // Bloqueo pesimista para el cliente del QR: serializa concurrencia por el mismo cliente
                Cliente cliente = clienteRepository.findByQrAccesoForUpdate(token)
                .orElseThrow(() -> {
                    log.error("Token QR no válido: {}", token);
                    return new IllegalArgumentException("Token QR no válido");
                });

        // 2. Validar que el QR esté activo
        if (!cliente.getQrActivo()) {
            log.warn("QR deshabilitado para cliente ID: {}", cliente.getId());
            return RegistrarAsistenciaResponse.builder()
                    .success(false)
                    .mensaje("❌ QR deshabilitado. Contacte al personal del gimnasio.")
                    .build();
        }

        // 3. Validar estado del cliente
        if (!"activo".equalsIgnoreCase(cliente.getEstado())) {
            log.warn("Cliente ID {} con estado inválido: {}", cliente.getId(), cliente.getEstado());
            String mensaje = switch (cliente.getEstado().toLowerCase()) {
                case "vencido" -> "❌ Membresía vencida. Por favor renueve su suscripción.";
                case "sin-membresia" -> "❌ No tiene membresía activa. Por favor adquiera una.";
                case "qr-deshabilitado" -> "❌ QR deshabilitado. Contacte al personal.";
                default -> "❌ Estado no válido. Contacte al personal del gimnasio.";
            };
            
            return RegistrarAsistenciaResponse.builder()
                    .success(false)
                    .mensaje(mensaje)
                    .cliente(mapClienteToDTO(cliente))
                    .build();
        }

        // 4. Verificar si ya registró asistencia hoy (robusto: rango inicio/fin del día)
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);
        var asistenciaHoy = asistenciaRepository
                .findFirstByClienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                        cliente.getId(), inicioDia, finDia);
        if (asistenciaHoy.isPresent()) {
            Asistencia asistenciaExistente = asistenciaHoy.get();
            log.warn("[DEDUPE] Cliente {} ya registró a las {} (ID={})",
                    cliente.getId(), asistenciaExistente.getFechaHora(), asistenciaExistente.getId());
            return RegistrarAsistenciaResponse.builder()
                    .success(false)
                    .mensaje("✅ Ya registraste tu asistencia hoy a las " + 
                            asistenciaExistente.getFechaHora().toLocalTime().toString())
                    .asistenciaId(asistenciaExistente.getId())
                    .fechaHora(asistenciaExistente.getFechaHora())
                    .cliente(mapClienteToDTO(cliente))
                    .build();
        }

        // 5. Registrar la asistencia
        Asistencia nuevaAsistencia = Asistencia.builder()
                .cliente(cliente)
                .tipoRegistro(TipoRegistro.QR_AUTO)
                .dispositivo(dispositivo)
                .ipAddress(ipAddress)
                .latitud(latitud)
                .longitud(longitud)
                .build();

        Asistencia guardada;
        try {
            guardada = asistenciaRepository.save(nuevaAsistencia);
        } catch (DataIntegrityViolationException ex) {
            // Posible colisión de unicidad por doble click/concurrencia.
            log.warn("[DEDUPE] Colisión de unicidad al registrar; consultando existente. Cliente {}", cliente.getId());
            var existente = asistenciaRepository
                    .findFirstByClienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                            cliente.getId(), inicioDia, finDia)
                    .orElse(null);
            if (existente != null) {
                log.warn("[DEDUPE] Devolviendo existente (ID={}, fechaHora={}) para Cliente {}",
                        existente.getId(), existente.getFechaHora(), cliente.getId());
                return RegistrarAsistenciaResponse.builder()
                        .success(false)
                        .mensaje("✅ Ya registraste tu asistencia hoy a las " + existente.getFechaHora().toLocalTime())
                        .asistenciaId(existente.getId())
                        .fechaHora(existente.getFechaHora())
                        .cliente(mapClienteToDTO(cliente))
                        .build();
            }
            throw ex;
        }
        log.info("✅ Asistencia registrada exitosamente - ID: {}, Cliente: {} {}",
                guardada.getId(), cliente.getNombre(), cliente.getApellido());

        return RegistrarAsistenciaResponse.builder()
                .success(true)
                .mensaje("✅ ¡Bienvenido/a " + cliente.getNombre() + "! Asistencia registrada exitosamente.")
                .asistenciaId(guardada.getId())
                .fechaHora(guardada.getFechaHora())
                .cliente(mapClienteToDTO(cliente))
                .build();
    }

    @Override
    @Transactional
    public RegistrarAsistenciaResponse registrarManual(
            Long clienteId,
            String notas,
            String dispositivo,
            String ipAddress
    ) {
        log.info("Registro manual de asistencia para cliente ID: {}", clienteId);

        // 1. Buscar cliente
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + clienteId));

        // 2. Verificar si ya registró hoy (robusto: rango inicio/fin del día)
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);
        var asistenciaHoy = asistenciaRepository
                .findFirstByClienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                        cliente.getId(), inicioDia, finDia);
        if (asistenciaHoy.isPresent()) {
            Asistencia existente = asistenciaHoy.get();
            log.warn("[DEDUPE] Cliente {} ya registró a las {} (ID={})",
                    clienteId, existente.getFechaHora(), existente.getId());
            return RegistrarAsistenciaResponse.builder()
                    .success(false)
                    .mensaje("Cliente ya registró asistencia hoy a las " + 
                            existente.getFechaHora().toLocalTime().toString())
                    .asistenciaId(existente.getId())
                    .fechaHora(existente.getFechaHora())
                    .cliente(mapClienteToDTO(cliente))
                    .build();
        }

        // 3. Registrar manualmente (sin validar estado - decisión del staff)
        Asistencia nuevaAsistencia = Asistencia.builder()
                .cliente(cliente)
                .tipoRegistro(TipoRegistro.MANUAL_STAFF)
                .dispositivo(dispositivo)
                .ipAddress(ipAddress)
                .notas(notas != null ? notas : "Registro manual por staff")
                .build();

        Asistencia guardada;
        try {
            guardada = asistenciaRepository.save(nuevaAsistencia);
        } catch (DataIntegrityViolationException ex) {
            log.warn("[DEDUPE] Colisión de unicidad en manual; consultando existente. Cliente {}", cliente.getId());
            var existente = asistenciaRepository
                    .findFirstByClienteIdAndFechaHoraBetweenOrderByFechaHoraDesc(
                            cliente.getId(), inicioDia, finDia)
                    .orElse(null);
            if (existente != null) {
                log.warn("[DEDUPE] Devolviendo existente (ID={}, fechaHora={}) para Cliente {}",
                        existente.getId(), existente.getFechaHora(), cliente.getId());
                return RegistrarAsistenciaResponse.builder()
                        .success(false)
                        .mensaje("Cliente ya registró asistencia hoy a las " + existente.getFechaHora().toLocalTime())
                        .asistenciaId(existente.getId())
                        .fechaHora(existente.getFechaHora())
                        .cliente(mapClienteToDTO(cliente))
                        .build();
            }
            throw ex;
        }
        log.info("✅ Asistencia manual registrada - ID: {}, Cliente: {} {}",
                guardada.getId(), cliente.getNombre(), cliente.getApellido());

        return RegistrarAsistenciaResponse.builder()
                .success(true)
                .mensaje("Asistencia registrada manualmente por el staff")
                .asistenciaId(guardada.getId())
                .fechaHora(guardada.getFechaHora())
                .cliente(mapClienteToDTO(cliente))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasHoy() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);
        
        List<Asistencia> asistencias = asistenciaRepository
                .findByFechaHoraBetweenOrderByFechaHoraDesc(inicioDia, finDia);
        
        log.info("Obtenidas {} asistencias del día {}", asistencias.size(), LocalDate.now());
        return asistencias.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorRango(LocalDate inicio, LocalDate fin) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);
        
        List<Asistencia> asistencias = asistenciaRepository
                .findByFechaHoraBetweenOrderByFechaHoraDesc(inicioDateTime, finDateTime);
        
        log.info("Obtenidas {} asistencias entre {} y {}", 
                asistencias.size(), inicio, fin);
        return asistencias.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerHistorialCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + clienteId);
        }
        
        List<Asistencia> asistencias = asistenciaRepository
                .findByClienteIdOrderByFechaHoraDesc(clienteId);
        
        log.info("Obtenidas {} asistencias del cliente ID {}", asistencias.size(), clienteId);
        return asistencias.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarAsistenciaHoy(Long clienteId) {
        var asistencia = asistenciaRepository.findAsistenciaHoyByClienteId(clienteId);
        return asistencia.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarAsistenciasMes(Long clienteId, int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mes inválido: " + mes);
        }
        
        Long count = asistenciaRepository.countAsistenciasByClienteAndMonth(clienteId, anio, mes);
        log.info("Cliente ID {} tiene {} asistencias en {}/{}", clienteId, count, mes, anio);
        return count;
    }

    @Override
    @Transactional
    public void eliminarAsistencia(Long id) {
        log.info("🗑️ Intentando eliminar asistencia ID: {}", id);
        
        // Verificar que la asistencia existe
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Asistencia no encontrada con ID: {}", id);
                    return new EntityNotFoundException("Asistencia no encontrada con ID: " + id);
                });
        
        // Eliminar la asistencia
        asistenciaRepository.delete(asistencia);
                // Asegurar visibilidad inmediata de la eliminación (evita falsos positivos al re-registrar en el mismo día)
                asistenciaRepository.flush();
        log.info("✅ Asistencia ID {} eliminada correctamente", id);
    }

        @Override
        @Transactional
        public int eliminarAsistenciasDeHoy(Long clienteId) {
                LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
                LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);
                int eliminadas = asistenciaRepository.deleteByClienteIdAndFechaHoraBetween(clienteId, inicioDia, finDia);
                log.info("🗑️ Eliminadas {} asistencias de hoy para cliente {} ({} - {})", eliminadas, clienteId, inicioDia, finDia);
                return eliminadas;
        }

    // ========== Métodos auxiliares de mapeo ==========

    private AsistenciaDTO mapToDTO(Asistencia asistencia) {
        Cliente cliente = asistencia.getCliente();
        
        AsistenciaDTO.ClienteBasicoDTO.ClienteBasicoDTOBuilder clienteBuilder = AsistenciaDTO.ClienteBasicoDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())  // ✅ Estado del cliente (activo/vencido/sin_membresia)
                .fechaVencimiento(cliente.getFechaVencimiento());  // ✅ Fecha de vencimiento

        // Agregar info de membresía si existe
        if (cliente.getMembresiaActual() != null) {
            clienteBuilder.membresia(
                AsistenciaDTO.MembresiaSimplifcadaDTO.builder()
                    .nombre(cliente.getMembresiaActual().getNombre())
                    .color(cliente.getMembresiaActual().getColor())
                    .build()
            );
        }
        
        return AsistenciaDTO.builder()
                .id(asistencia.getId())
                .fechaHora(asistencia.getFechaHora())
                .tipoRegistro(asistencia.getTipoRegistro())
                .dispositivo(asistencia.getDispositivo())
                .ipAddress(asistencia.getIpAddress())
                .latitud(asistencia.getLatitud())
                .longitud(asistencia.getLongitud())
                .notas(asistencia.getNotas())
                .cliente(clienteBuilder.build())
                .build();
    }

    private RegistrarAsistenciaResponse.ClienteAsistenciaDTO mapClienteToDTO(Cliente cliente) {
        RegistrarAsistenciaResponse.ClienteAsistenciaDTO.ClienteAsistenciaDTOBuilder builder = 
            RegistrarAsistenciaResponse.ClienteAsistenciaDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .estado(cliente.getEstado());

        // Agregar info de membresía si existe
        if (cliente.getMembresiaActual() != null) {
            builder.membresia(
                RegistrarAsistenciaResponse.MembresiaSimplifcadaDTO.builder()
                    .nombre(cliente.getMembresiaActual().getNombre())
                    .color(cliente.getMembresiaActual().getColor())
                    .build()
            );
        }

        return builder.build();
    }
}
