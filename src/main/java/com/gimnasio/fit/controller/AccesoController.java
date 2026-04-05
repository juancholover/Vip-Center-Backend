package com.gimnasio.fit.controller;

import com.gimnasio.fit.service.AccesoService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/acceso")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AccesoController {

    private final AccesoService accesoService;

    /**
     * Verificar si un cliente puede acceder al gimnasio usando token QR
     * GET /api/acceso/verificar-qr/{qrToken}
     */
    @GetMapping("/verificar-qr/{qrToken}")
    public ResponseEntity<VerificarAccesoResponse> verificarAccesoConQR(@PathVariable String qrToken) {
        log.info("🔍 Verificando acceso con QR token: {}", qrToken.substring(0, Math.min(8, qrToken.length())) + "...");
        
        VerificarAccesoResponse response = accesoService.verificarAccesoConQR(qrToken);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verificar si un cliente puede acceder al gimnasio (legacy - por ID)
     * GET /api/acceso/verificar/{clienteId}
     */
    @GetMapping("/verificar/{clienteId}")
    public ResponseEntity<VerificarAccesoResponse> verificarAcceso(@PathVariable Long clienteId) {
        log.info("🔍 Verificando acceso para cliente ID: {}", clienteId);
        
        VerificarAccesoResponse response = accesoService.verificarAcceso(clienteId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Registrar asistencia de un cliente usando token QR
     * POST /api/acceso/registrar-qr
     */
    @PostMapping("/registrar-qr")
    public ResponseEntity<RegistrarAsistenciaResponse> registrarAsistenciaConQR(
            @RequestBody RegistrarAsistenciaConQRRequest request) {
        
        log.info("📝 Registrando asistencia con QR - Tipo: {}", request.getTipoRegistro());
        
        RegistrarAsistenciaResponse response = accesoService.registrarAsistenciaConQR(
                request.getQrToken(),
                request.getTipoRegistro(),
                request.getEmpleadoId()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Registrar asistencia de un cliente (legacy - por ID)
     * POST /api/acceso/registrar
     */
    @PostMapping("/registrar")
    public ResponseEntity<RegistrarAsistenciaResponse> registrarAsistencia(
            @RequestBody RegistrarAsistenciaRequest request) {
        
        log.info("📝 Registrando asistencia para cliente ID: {} - Tipo: {}", 
                request.getClienteId(), request.getTipoRegistro());
        
        RegistrarAsistenciaResponse response = accesoService.registrarAsistencia(
                request.getClienteId(),
                request.getTipoRegistro(),
                request.getEmpleadoId()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener las últimas asistencias registradas
     * GET /api/acceso/asistencias-recientes?limite=5
     */
    @GetMapping("/asistencias-recientes")
    public ResponseEntity<List<AsistenciaRecienteDTO>> obtenerAsistenciasRecientes(
            @RequestParam(defaultValue = "5") int limite) {
        
        log.info("📋 Obteniendo últimas {} asistencias", limite);
        
        List<AsistenciaRecienteDTO> asistencias = accesoService.obtenerAsistenciasRecientes(limite);
        
        return ResponseEntity.ok(asistencias);
    }

    /**
     * Buscar clientes por nombre o teléfono
     * GET /api/acceso/buscar-cliente?query=Carlos
     */
    @GetMapping("/buscar-cliente")
    public ResponseEntity<List<ClienteBusquedaDTO>> buscarCliente(@RequestParam String query) {
        log.info("🔎 Buscando clientes con query: {}", query);
        
        List<ClienteBusquedaDTO> resultados = accesoService.buscarClientes(query);
        
        return ResponseEntity.ok(resultados);
    }

    /**
     * Obtener contador de ingresos del día
     * GET /api/acceso/contador-dia
     */
    @GetMapping("/contador-dia")
    public ResponseEntity<Map<String, Integer>> obtenerContadorDia() {
        int total = accesoService.contarIngresosDia();
        return ResponseEntity.ok(Map.of("total", total));
    }

    // ===== DTOs =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificarAccesoResponse {
        private boolean accesoPermitido;
        private String motivo;
        private ClienteAccesoDTO cliente;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteAccesoDTO {
        private Long id;
        private String nombre;
        private String apellido;
        private String nombreCompleto;
        private String foto;
        private MembresiaDTO membresia;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembresiaDTO {
        private String tipo;
        private String fechaVencimiento;
        private String estado;
        private Integer diasRestantes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrarAsistenciaConQRRequest {
        private String qrToken;
        private String tipoRegistro; // "QR_AUTO" o "MANUAL_STAFF"
        private Long empleadoId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrarAsistenciaRequest {
        private Long clienteId;
        private String tipoRegistro; // "QR_AUTO" o "MANUAL_STAFF"
        private Long empleadoId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrarAsistenciaResponse {
        private Long id;
        private Long clienteId;
        private LocalDateTime fechaHora;
        private String tipoRegistro;
        private String estado;
        private String mensaje;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsistenciaRecienteDTO {
        private Long id;
        private ClienteResumeDTO cliente;
        private LocalDateTime fechaHora;
        private String tipoRegistro;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteResumeDTO {
        private Long id;
        private String nombreCompleto;
        private String membresiaTipo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteBusquedaDTO {
        private Long id;
        private String nombre;
        private String apellido;
        private String nombreCompleto;
        private String telefono;
        private String foto;
        private MembresiaSimpleDTO membresia;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembresiaSimpleDTO {
        private String tipo;
        private String estado;
        private String fechaVencimiento;
    }
}
