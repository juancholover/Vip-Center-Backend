package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.ActualizarClienteRequest;
import com.gimnasio.fit.dto.ClienteResponse;
import com.gimnasio.fit.dto.CrearClienteRequest;
import com.gimnasio.fit.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * API REST para gestión de clientes del gimnasio.
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Crea un nuevo cliente.
     * El usuario autenticado será registrado como quien creó al cliente.
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(
            @Valid @RequestBody CrearClienteRequest request,
            Authentication authentication) {

        // 👤 Obtener ID del usuario autenticado
        Long registradoPorId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            registradoPorId = clienteService.obtenerIdUsuarioPorEmail(email);
        }

        ClienteResponse response = clienteService.crear(request, registradoPorId);
        return ResponseEntity
                .created(URI.create("/api/clientes/" + response.getId()))
                .body(response);
    }

    /**
     * Lista todos los clientes.
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        List<ClienteResponse> clientes = clienteService.listarTodos();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    /**
     * Busca un cliente por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) { // 🔹 cambiado a Long
        ClienteResponse response = clienteService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un cliente existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Long id, // 🔹 cambiado a Long
            @Valid @RequestBody ActualizarClienteRequest request) {
        ClienteResponse response = clienteService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un cliente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { // 🔹 cambiado a Long
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca clientes por nombre o apellido.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscarPorNombre(
            @RequestParam String termino) {
        List<ClienteResponse> clientes = clienteService.buscarPorNombreOApellido(termino);
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    /**
     * Busca un cliente por teléfono.
     */
    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<ClienteResponse> buscarPorTelefono(@PathVariable String telefono) {
        ClienteResponse response = clienteService.buscarPorTelefono(telefono);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca un cliente por código QR.
     */
    @GetMapping("/qr/{qr}")
    public ResponseEntity<ClienteResponse> buscarPorQr(@PathVariable String qr) {
        ClienteResponse response = clienteService.buscarPorQr(qr);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista clientes por estado (activos, vencidos, etc.)
     */
    @GetMapping("/filtro/{tipo}")
    public ResponseEntity<List<ClienteResponse>> listarPorFiltro(
            @PathVariable String tipo,
            @RequestParam(required = false, defaultValue = "7") int dias) {

        List<ClienteResponse> clientes = switch (tipo.toLowerCase()) {
            case "activos" -> clienteService.listarActivos();
            case "vencidos" -> clienteService.listarVencidos();
            case "sin-membresia" -> clienteService.listarSinMembresia();
            case "proximos-vencer" -> clienteService.listarProximosAVencer(dias);
            default -> throw new IllegalArgumentException(
                    "Tipo de filtro inválido. Valores permitidos: activos, vencidos, sin-membresia, proximos-vencer");
        };

        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    /**
     * Regenera el código QR de acceso de un cliente.
     * Útil cuando el cliente pierde o reporta robo de su tarjeta/credencial.
     */
    @PostMapping("/{id}/regenerar-qr")
    public ResponseEntity<ClienteResponse> regenerarQr(@PathVariable Long id) {
        ClienteResponse response = clienteService.regenerarQr(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Corrige todos los clientes que no tienen QR de acceso generándoles uno automáticamente.
     * 🔧 Endpoint de mantenimiento - Solo debe usarse en casos especiales.
     */
    @PostMapping("/corregir-qr")
    public ResponseEntity<String> corregirClientesSinQr() {
        int corregidos = clienteService.corregirClientesSinQr();
        if (corregidos == 0) {
            return ResponseEntity.ok("✅ Todos los clientes ya tienen QR de acceso generado");
        }
        return ResponseEntity.ok(String.format("✅ Se corrigieron %d clientes sin QR", corregidos));
    }
}
