package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.controller.AccesoController.*;
import com.gimnasio.fit.entity.Asistencia;
import com.gimnasio.fit.entity.Cliente;
import com.gimnasio.fit.entity.Membresia;
import com.gimnasio.fit.entity.TipoRegistro;
import com.gimnasio.fit.repository.AsistenciaRepository;
import com.gimnasio.fit.repository.ClienteRepository;
import com.gimnasio.fit.service.AccesoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccesoServiceImpl implements AccesoService {

    private final ClienteRepository clienteRepository;
    private final AsistenciaRepository asistenciaRepository;

    @Override
    @Transactional(readOnly = true)
    public VerificarAccesoResponse verificarAccesoConQR(String qrToken) {
        try {
            // Buscar cliente por token QR
            Cliente cliente = clienteRepository.findByQrAcceso(qrToken)
                    .orElse(null);

            if (cliente == null) {
                log.warn("⚠️ Cliente no encontrado con QR: {}", qrToken.substring(0, Math.min(8, qrToken.length())) + "...");
                return new VerificarAccesoResponse(
                        false,
                        "QR_INVALIDO",
                        null
                );
            }

            // Delegar al método existente que verifica por ID
            return verificarAcceso(cliente.getId());

        } catch (Exception e) {
            log.error("❌ Error al verificar acceso con QR: {}", e.getMessage(), e);
            return new VerificarAccesoResponse(
                    false,
                    "ERROR_SISTEMA",
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VerificarAccesoResponse verificarAcceso(Long clienteId) {
        try {
            // Buscar cliente
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElse(null);

            if (cliente == null) {
                log.warn("⚠️ Cliente no encontrado: {}", clienteId);
                return new VerificarAccesoResponse(
                        false,
                        "CLIENTE_NO_ENCONTRADO",
                        null
                );
            }

            // Verificar si está activo
            if (!cliente.getQrActivo()) {
                log.warn("⚠️ QR deshabilitado para cliente: {}", clienteId);
                return new VerificarAccesoResponse(
                        false,
                        "QR_DESHABILITADO",
                        construirClienteDTO(cliente, "QR_DESHABILITADO", null)
                );
            }

            // Verificar membresía
            Membresia membresia = cliente.getMembresiaActual();
            if (membresia == null) {
                log.warn("⚠️ Cliente sin membresía: {}", clienteId);
                return new VerificarAccesoResponse(
                        false,
                        "SIN_MEMBRESIA",
                        construirClienteDTO(cliente, "SIN_MEMBRESIA", null)
                );
            }

            // Verificar fecha de vencimiento
            LocalDate hoy = LocalDate.now();
            LocalDate fechaVencimiento = cliente.getFechaVencimiento();

            if (fechaVencimiento == null || fechaVencimiento.isBefore(hoy)) {
                log.warn("⚠️ Membresía vencida para cliente: {} - Venció: {}", clienteId, fechaVencimiento);
                return new VerificarAccesoResponse(
                        false,
                        "MEMBRESIA_VENCIDA",
                        construirClienteDTO(cliente, "VENCIDA", fechaVencimiento)
                );
            }

            // Acceso permitido
            log.info("✅ Acceso permitido para cliente: {} - Membresía: {} - Vence: {}",
                    cliente.getNombreCompleto(), membresia.getNombre(), fechaVencimiento);

            return new VerificarAccesoResponse(
                    true,
                    "ACCESO_PERMITIDO",
                    construirClienteDTO(cliente, "ACTIVA", fechaVencimiento)
            );

        } catch (Exception e) {
            log.error("❌ Error al verificar acceso: {}", e.getMessage(), e);
            return new VerificarAccesoResponse(
                    false,
                    "ERROR_SISTEMA",
                    null
            );
        }
    }

    @Override
    @Transactional
    public RegistrarAsistenciaResponse registrarAsistenciaConQR(String qrToken, String tipoRegistro, Long empleadoId) {
        try {
            // Buscar cliente por token QR
            Cliente cliente = clienteRepository.findByQrAcceso(qrToken)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con QR: " + qrToken));

            // Delegar al método existente que registra por ID
            return registrarAsistencia(cliente.getId(), tipoRegistro, empleadoId);

        } catch (Exception e) {
            log.error("❌ Error al registrar asistencia con QR: {}", e.getMessage(), e);
            throw new RuntimeException("Error al registrar asistencia con QR: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RegistrarAsistenciaResponse registrarAsistencia(Long clienteId, String tipoRegistro, Long empleadoId) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            // Convertir String a Enum
            TipoRegistro tipoEnum = TipoRegistro.valueOf(tipoRegistro);
            
            Asistencia asistencia = new Asistencia();
            asistencia.setCliente(cliente);
            asistencia.setFechaHora(LocalDateTime.now());
            asistencia.setTipoRegistro(tipoEnum);

            Asistencia guardada = asistenciaRepository.save(asistencia);

            log.info("✅ Asistencia registrada: Cliente {} - Tipo: {} - Hora: {}",
                    cliente.getNombreCompleto(), tipoRegistro, guardada.getFechaHora());

            return new RegistrarAsistenciaResponse(
                    guardada.getId(),
                    guardada.getCliente().getId(),
                    guardada.getFechaHora(),
                    guardada.getTipoRegistro().name(),
                    "REGISTRADO",
                    "Asistencia registrada exitosamente"
            );

        } catch (Exception e) {
            log.error("❌ Error al registrar asistencia: {}", e.getMessage(), e);
            throw new RuntimeException("Error al registrar asistencia: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaRecienteDTO> obtenerAsistenciasRecientes(int limite) {
        try {
            PageRequest pageRequest = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fechaHora"));
            List<Asistencia> asistencias = asistenciaRepository.findAll(pageRequest).getContent();

            return asistencias.stream()
                    .map(a -> new AsistenciaRecienteDTO(
                            a.getId(),
                            new ClienteResumeDTO(
                                    a.getCliente().getId(),
                                    a.getCliente().getNombreCompleto(),
                                    a.getCliente().getMembresiaActual() != null ?
                                            a.getCliente().getMembresiaActual().getNombre() : "Sin membresía"
                            ),
                            a.getFechaHora(),
                            a.getTipoRegistro().name()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error al obtener asistencias recientes: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteBusquedaDTO> buscarClientes(String query) {
        try {
            String queryLower = query.toLowerCase();

            List<Cliente> clientes = clienteRepository.findAll().stream()
                    .filter(c -> c.getQrActivo() &&
                            (c.getNombreCompleto().toLowerCase().contains(queryLower) ||
                             (c.getTelefono() != null && c.getTelefono().contains(query))))
                    .limit(10)
                    .collect(Collectors.toList());

            return clientes.stream()
                    .map(c -> {
                        MembresiaSimpleDTO membresiaDTO = null;
                        if (c.getMembresiaActual() != null) {
                            String estado = "SIN_MEMBRESIA";
                            LocalDate fechaVenc = c.getFechaVencimiento();
                            
                            if (fechaVenc != null) {
                                estado = fechaVenc.isBefore(LocalDate.now()) ? "VENCIDA" : "ACTIVA";
                            }

                            membresiaDTO = new MembresiaSimpleDTO(
                                    c.getMembresiaActual().getNombre(),
                                    estado,
                                    fechaVenc != null ? fechaVenc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null
                            );
                        }

                        return new ClienteBusquedaDTO(
                                c.getId(),
                                c.getNombre(),
                                c.getApellido(),
                                c.getNombreCompleto(),
                                c.getTelefono(),
                                null, // foto
                                membresiaDTO
                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error al buscar clientes: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int contarIngresosDia() {
        try {
            LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
            LocalDateTime finDia = inicioDia.plusDays(1);

            return (int) asistenciaRepository.countByFechaHoraBetween(inicioDia, finDia);

        } catch (Exception e) {
            log.error("❌ Error al contar ingresos del día: {}", e.getMessage(), e);
            return 0;
        }
    }

    // ===== Métodos auxiliares =====

    private ClienteAccesoDTO construirClienteDTO(Cliente cliente, String estadoMembresia, LocalDate fechaVencimiento) {
        MembresiaDTO membresiaDTO = null;

        if (cliente.getMembresiaActual() != null) {
            Integer diasRestantes = null;
            String fechaVencStr = null;

            if (fechaVencimiento != null) {
                diasRestantes = (int) ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
                fechaVencStr = fechaVencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            membresiaDTO = new MembresiaDTO(
                    cliente.getMembresiaActual().getNombre(),
                    fechaVencStr,
                    estadoMembresia,
                    diasRestantes
            );
        }

        return new ClienteAccesoDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getNombreCompleto(),
                null, // foto - implementar después si es necesario
                membresiaDTO
        );
    }
}
