package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.dto.ActualizarClienteRequest;
import com.gimnasio.fit.dto.ClienteResponse;
import com.gimnasio.fit.dto.CrearClienteRequest;
import com.gimnasio.fit.entity.Cliente;
import com.gimnasio.fit.repository.ClienteRepository;
import com.gimnasio.fit.repository.AsistenciaRepository;
import com.gimnasio.fit.repository.UsuarioRepository;
import com.gimnasio.fit.service.ClienteService;
import com.gimnasio.fit.util.QRCodeGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    // =====================================================
    // 🟢 CREAR CLIENTE
    // =====================================================
    @Override
    @Transactional
    public ClienteResponse crear(CrearClienteRequest request, Long registradoPorId) {
        // 🔧 Normalizar teléfono: eliminar código de país y caracteres no numéricos
        String telefonoNormalizado = normalizarTelefono(request.getTelefono());
        
        // Validar duplicado
        if (clienteRepository.existsByNombreAndApellidoAndTelefono(
                request.getNombre(),
                request.getApellido(),
                telefonoNormalizado)) {
            throw new IllegalArgumentException(String.format(
                    "Ya existe un cliente con nombre '%s %s' y teléfono '%s'",
                    request.getNombre(), request.getApellido(), telefonoNormalizado));
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setTelefono(telefonoNormalizado);
        cliente.setEmail(request.getEmail());
        
        // 🔑 GENERAR QR DE ACCESO OPTIMIZADO (UUID sin guiones para QR más simple)
        cliente.setQrAcceso(QRCodeGenerator.generateOptimizedQR());
        cliente.setQrActivo(true); // por defecto activo

        if (registradoPorId != null) {
            usuarioRepository.findById(registradoPorId)
                    .ifPresent(cliente::setRegistradoPor);
        }

        Cliente guardado = clienteRepository.save(cliente);
        return mapearAResponse(guardado);
    }

    // =====================================================
    // 🟡 ACTUALIZAR CLIENTE
    // =====================================================
    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ActualizarClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));

        if (request.getNombre() != null) cliente.setNombre(request.getNombre());
        if (request.getApellido() != null) cliente.setApellido(request.getApellido());

        if (request.getTelefono() != null) {
            // 🔧 Normalizar teléfono: eliminar código de país y caracteres no numéricos
            String telefonoNormalizado = normalizarTelefono(request.getTelefono());
            
            if (!telefonoNormalizado.equals(cliente.getTelefono()) &&
                    clienteRepository.existsByNombreAndApellidoAndTelefono(
                            cliente.getNombre(),
                            cliente.getApellido(),
                            telefonoNormalizado)) {
                throw new IllegalArgumentException("Ya existe otro cliente con esa combinación de nombre y teléfono");
            }
            cliente.setTelefono(telefonoNormalizado);
        }

        if (request.getEmail() != null) cliente.setEmail(request.getEmail());

        Cliente actualizado = clienteRepository.save(cliente);
        return mapearAResponse(actualizado);
    }

    // =====================================================
    // 🔍 BÚSQUEDAS
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
        return mapearAResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorTelefono(String telefono) {
        // 🔧 Normalizar teléfono antes de buscar
        String telefonoNormalizado = normalizarTelefono(telefono);
        Cliente cliente = clienteRepository.findByTelefono(telefonoNormalizado)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con teléfono: " + telefonoNormalizado));
        return mapearAResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorQr(String qrAcceso) {
        Cliente cliente = clienteRepository.findByQrAcceso(qrAcceso)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con QR: " + qrAcceso));
        return mapearAResponse(cliente);
    }

    // =====================================================
    // 📋 LISTADOS
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorNombreOApellido(String termino) {
        return clienteRepository.buscarPorNombreOApellido(termino).stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAllWithRegistrador().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarActivos() {
        LocalDate hoy = LocalDate.now();
        return clienteRepository.listarActivos(hoy).stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarVencidos() {
        LocalDate hoy = LocalDate.now();
        return clienteRepository.listarVencidos(hoy).stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarSinMembresia() {
        return clienteRepository.listarSinMembresia().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarProximosAVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);
        return clienteRepository.listarProximosAVencer(hoy, fechaLimite).stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 🔴 ELIMINAR CLIENTE
    // =====================================================
    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    // =====================================================
    // 🔄 REGENERAR QR DE ACCESO
    // =====================================================
    @Override
    @Transactional
    public ClienteResponse regenerarQr(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
        
        // Generar nuevo QR optimizado (UUID sin guiones)
        String nuevoQr = QRCodeGenerator.generateOptimizedQR();
        cliente.setQrAcceso(nuevoQr);
        cliente.setQrActivo(true);
        
        Cliente actualizado = clienteRepository.save(cliente);
        return mapearAResponse(actualizado);
    }

    // =====================================================
    // 🔧 CORREGIR CLIENTES SIN QR
    // =====================================================
    @Override
    @Transactional
    public int corregirClientesSinQr() {
        List<Cliente> clientesSinQr = clienteRepository.findAll().stream()
                .filter(c -> c.getQrAcceso() == null || c.getQrAcceso().isEmpty())
                .toList();
        
        if (clientesSinQr.isEmpty()) {
            return 0;
        }
        
        // Generar QR optimizado para cada cliente que no lo tiene
        clientesSinQr.forEach(cliente -> {
            cliente.setQrAcceso(QRCodeGenerator.generateOptimizedQR());
            cliente.setQrActivo(true);
        });
        
        clienteRepository.saveAll(clientesSinQr);
        
        return clientesSinQr.size();
    }

    @Override
    public Long obtenerIdUsuarioPorEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        
        return usuarioRepository.findByEmail(email)
                .map(com.gimnasio.fit.entity.Usuario::getId)
                .orElse(null);
    }

    // =====================================================
    // � NORMALIZACIÓN DE TELÉFONO
    // =====================================================
    /**
     * Normaliza el teléfono eliminando:
     * - Código de país (+51, +1, etc.)
     * - Espacios, guiones, paréntesis
     * - Solo deja los dígitos
     * 
     * Ejemplos:
     * - "+48884545" → "48884545"
     * - "+51 999 888 777" → "999888777"
     * - "(01) 234-5678" → "012345678"
     */
    private String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.isEmpty()) {
            return telefono;
        }
        
        // Eliminar todos los caracteres no numéricos
        String soloDigitos = telefono.replaceAll("[^0-9]", "");
        
        // Si empieza con código de país común (51, 1, 52, 34, etc.)
        // y tiene más de 9 dígitos, eliminar el código
        if (soloDigitos.length() > 9) {
            // Códigos de país comunes de 1-3 dígitos
            // Perú: 51, USA: 1, México: 52, España: 34, Colombia: 57, etc.
            if (soloDigitos.startsWith("51")) {
                soloDigitos = soloDigitos.substring(2); // Eliminar "51"
            } else if (soloDigitos.startsWith("1") && soloDigitos.length() == 11) {
                soloDigitos = soloDigitos.substring(1); // Eliminar "1" (USA/Canada)
            } else if (soloDigitos.startsWith("52")) {
                soloDigitos = soloDigitos.substring(2); // Eliminar "52" (México)
            } else if (soloDigitos.startsWith("34") || soloDigitos.startsWith("57")) {
                soloDigitos = soloDigitos.substring(2); // Eliminar código de 2 dígitos
            }
        }
        
        return soloDigitos;
    }

    // =====================================================
    // �🧩 MÉTODO AUXILIAR DE MAPEADO
    // =====================================================
    private ClienteResponse mapearAResponse(Cliente cliente) {
    ClienteResponse.ClienteResponseBuilder builder = ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .nombreCompleto(cliente.getNombreCompleto())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .estado(cliente.getEstado())
                .fechaVencimiento(cliente.getFechaVencimiento())
                .qrActivo(cliente.getQrActivo())
                .qrAcceso(cliente.getQrAcceso())
                .fechaRegistro(cliente.getFechaRegistro());

    // Consultar última asistencia (puede ser null si no tiene)
    var ultima = asistenciaRepository.findUltimaAsistenciaByClienteId(cliente.getId());
    builder.ultimaAsistencia(ultima);

        if (cliente.getRegistradoPor() != null) {
            builder.registradoPor(
                    cliente.getRegistradoPor().getNombre() + " " +
                            cliente.getRegistradoPor().getApellido());
        }

        // Agregar información de membresía actual si existe
        if (cliente.getMembresiaActual() != null) {
            builder.membresiaActual(
                ClienteResponse.MembresiaInfoDTO.builder()
                    .id(cliente.getMembresiaActual().getId())
                    .nombre(cliente.getMembresiaActual().getNombre())
                    .duracionDias(cliente.getMembresiaActual().getDuracionDias())
                    .color(cliente.getMembresiaActual().getColor())
                    .build()
            );
        }

        return builder.build();
    }
}
