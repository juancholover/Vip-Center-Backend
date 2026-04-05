package com.gimnasio.fit.controller;

import com.gimnasio.fit.dto.PagoRequestDto;
import com.gimnasio.fit.dto.PagoResponseDto;
import com.gimnasio.fit.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Slf4j
public class PagoController {

    private final PagoService pagoService;

    // ✅ Crear preferencia de pago
    @PostMapping("/crear")
    public ResponseEntity<?> crearPago(@Valid @RequestBody PagoRequestDto dto) {
        try {
            log.info("📥 crearPago request body: {}", dto);
            PagoResponseDto response = pagoService.crearPreferencia(dto);
            log.info("💰 Preferencia creada para cliente {}: {}", dto.getClienteId(), response.getPreferenceId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al crear preferencia de pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno al crear el pago", e.getMessage()));
        }
    }

    // ✅ Crear pago con Yape (directo, usando token del frontend)
    @PostMapping("/crear-con-yape")
    public ResponseEntity<?> crearPagoConYape(@Valid @RequestBody com.gimnasio.fit.dto.PagoYapeRequestDto dto) {
        try {
            log.info("📥 crearPagoConYape request body: clienteId={}, monto={}", dto.getClienteId(), dto.getMonto());
            PagoResponseDto response = pagoService.crearPagoConYape(dto);
            log.info("💰 Pago Yape creado para cliente {}: status={}", dto.getClienteId(), response.getStatus());
            
            // Retornar 200 si aprobado, 402 si rechazado, 202 si pendiente
            if ("approved".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else if ("rejected".equals(response.getStatus())) {
                return ResponseEntity.status(402).body(response); // Payment Required
            } else {
                return ResponseEntity.accepted().body(response); // 202 Accepted (pendiente/en proceso)
            }

        } catch (Exception e) {
            log.error("❌ Error al crear pago con Yape: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno al procesar pago con Yape", e.getMessage()));
        }
    }

    // ✅ Endpoint para recibir notificaciones (webhook)
    @PostMapping("/webhook")
    public ResponseEntity<String> recibirWebhook(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String action,
            @RequestParam(required = false, name = "id") String paymentId,
            @RequestBody(required = false) String body
    ) {
        try {
            log.info("📩 Webhook recibido:");
            log.info("topic: {}", topic);
            log.info("action: {}", action);
            log.info("paymentId: {}", paymentId);
            log.info("body: {}", body);

            pagoService.procesarWebhook(topic, action, paymentId, body);
            return ResponseEntity.ok("✅ Webhook procesado correctamente");

        } catch (Exception e) {
            log.error("❌ Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno procesando webhook: " + e.getMessage());
        }
    }

    // ✅ Endpoint para devolver la imagen PNG del QR dado el initPoint
    @GetMapping(value = "/{preferenceId}/qr", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> obtenerQr(@PathVariable String preferenceId,
                                            @RequestParam(required = false) String initPoint) {
        try {
            if (initPoint == null || initPoint.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] png = pagoService.generarQrPng(initPoint);
            if (png == null) return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();

            return ResponseEntity.ok().body(png);
        } catch (Exception e) {
            log.error("Error al generar QR: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Endpoint para obtener datos de la preferencia por preferenceId
    @GetMapping("/preferencias/{preferenceId}")
    public ResponseEntity<?> obtenerPreferencia(@PathVariable String preferenceId) {
        try {
            var preferencia = pagoService.obtenerPreferenciaPorId(preferenceId);
            log.info("📄 Preferencia recuperada: {}", preferenceId);
            
            // Devolver datos relevantes
            return ResponseEntity.ok(new PreferenciaResponse(
                preferencia.getPreferenceId(),
                preferencia.getInitPoint(),
                preferencia.getCliente().getId(),
                preferencia.getCliente().getNombreCompleto(),
                preferencia.getMonto(),
                preferencia.getPlanNombre(),
                preferencia.getPlanDias(),
                preferencia.getEstado()
            ));
        } catch (Exception e) {
            log.error("❌ Error al obtener preferencia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Preferencia no encontrada", e.getMessage()));
        }
    }

    // ✅ Endpoint para obtener el link de WhatsApp preformateado
    @GetMapping("/preferencias/{preferenceId}/whatsapp")
    public ResponseEntity<?> obtenerWhatsAppLink(@PathVariable String preferenceId) {
        try {
            var preferencia = pagoService.obtenerPreferenciaPorId(preferenceId);
            String whatsappLink = pagoService.generarWhatsAppLink(preferencia);
            
            log.info("📱 WhatsApp link generado para preferencia: {}", preferenceId);
            return ResponseEntity.ok(new WhatsAppResponse(whatsappLink, preferencia.getInitPoint()));
            
        } catch (Exception e) {
            log.error("❌ Error al generar WhatsApp link: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Preferencia no encontrada", e.getMessage()));
        }
    }

    // ✅ Clase auxiliar para respuestas de error legibles
    record ErrorResponse(String error, String detalle) {}
    
    // ✅ Respuesta de datos de preferencia
    record PreferenciaResponse(
        String preferenceId,
        String initPoint,
        Long clienteId,
        String clienteNombre,
        java.math.BigDecimal monto,
        String planNombre,
        Integer planDias,
        String estado
    ) {}
    
    // ✅ Respuesta con link de WhatsApp
    record WhatsAppResponse(String whatsappLink, String initPoint) {}

    // ✅ Procesar reembolso de pago
    @PostMapping("/{pagoId}/reembolso")
    public ResponseEntity<?> procesarReembolso(
            @PathVariable Long pagoId,
            @Valid @RequestBody com.gimnasio.fit.dto.ReembolsoRequestDto dto) {
        try {
            log.info("💰 Procesando reembolso para pago: {}", pagoId);
            
            com.gimnasio.fit.dto.ReembolsoResponseDto response = 
                    pagoService.procesarReembolso(pagoId, dto.getMotivo(), dto.getMonto());
            
            log.info("✅ Reembolso procesado: refundId={}", response.getRefundId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al procesar reembolso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al procesar reembolso", e.getMessage()));
        }
    }

    // ✅ Verificar pago manual (Yape QR, Efectivo, Transferencia)
    @PostMapping("/{clienteId}/verificar-pago-manual")
    public ResponseEntity<?> verificarPagoManual(
            @PathVariable Long clienteId,
            @Valid @RequestBody VerificarPagoManualRequest request) {
        try {
            log.info("✅ Verificando pago manual para cliente: {}, membresía: {}, días: {}", 
                    clienteId, request.membresiaId(), request.planDias());
            
            var response = pagoService.verificarPagoManual(
                    clienteId, 
                    request.membresiaId(), 
                    request.planDias(),
                    request.monto(),
                    request.planNombre()
            );
            
            log.info("✅ Pago manual verificado exitosamente para cliente: {}", clienteId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al verificar pago manual: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al verificar pago manual", e.getMessage()));
        }
    }

    // ✅ Request para verificación manual
    record VerificarPagoManualRequest(
        Long membresiaId,
        Integer planDias,
        Double monto,
        String planNombre
    ) {}
    
}
