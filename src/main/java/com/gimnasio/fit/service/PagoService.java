package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.PagoRequestDto;
import com.gimnasio.fit.dto.PagoResponseDto;
import com.gimnasio.fit.dto.PagoYapeRequestDto;
import com.gimnasio.fit.entity.Cliente;
import com.gimnasio.fit.entity.Pago;
import com.gimnasio.fit.entity.PreferenciaMP;
import com.gimnasio.fit.repository.ClienteRepository;
import com.gimnasio.fit.repository.PagoRepository;
import com.gimnasio.fit.repository.PreferenciaMPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

import java.time.LocalDate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    @Value("${mercadopago.access.token}")
    private String mercadoPagoToken;

    private final PreferenciaMPRepository preferenciaMPRepository;
    private final ClienteRepository clienteRepository;
    private final PagoRepository pagoRepository;
    private final NotificationService notificationService;
    private final com.gimnasio.fit.repository.MembresiaRepository membresiaRepository;
    private final com.gimnasio.fit.repository.UsuarioRepository usuarioRepository;
    private final com.gimnasio.fit.repository.RenovacionRepository renovacionRepository;

    public PagoResponseDto crearPreferencia(PagoRequestDto dto) throws Exception {
        try {
            log.info("📥 crearPreferencia - ClienteId: {}, MembresiaId: {}, Monto: {}, Plan: {}", 
                    dto.getClienteId(), dto.getMembresiaId(), dto.getMonto(), dto.getPlanNombre());
            // 🔐 Configurar credencial
            MercadoPagoConfig.setAccessToken(mercadoPagoToken);

            // 🧾 Crear ítem
            String titulo = dto.getDescripcion();
            if (titulo == null || titulo.isBlank()) {
                titulo = dto.getPlanNombre() != null ? dto.getPlanNombre() : "Pago";
            }

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(titulo)
                    .quantity(1)
                    .unitPrice(new BigDecimal(dto.getMonto()))
                    .currencyId("PEN")
                    .build();

            // 🧩 Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Generar QR a partir del initPoint (si existe)
            String initPoint = preference.getInitPoint();
            String qrBase64 = null;
            if (initPoint != null && !initPoint.isBlank()) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    int width = 250;
                    int height = 250;
                    BitMatrix matrix = new MultiFormatWriter().encode(initPoint, BarcodeFormat.QR_CODE, width, height);
                    BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
                    javax.imageio.ImageIO.write(image, "PNG", baos);
                    qrBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                } catch (WriterException we) {
                    log.warn("No fue posible generar QR (WriterException): {}", we.getMessage());
                } catch (Exception e) {
                    log.warn("No fue posible generar QR: {}", e.getMessage());
                }
            }

            // 💾 Persistir preferencia en BD
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + dto.getClienteId()));

            PreferenciaMP preferenciaMP = new PreferenciaMP();
            preferenciaMP.setPreferenceId(preference.getId());
            preferenciaMP.setInitPoint(initPoint);
            preferenciaMP.setCliente(cliente);
            preferenciaMP.setMonto(new BigDecimal(dto.getMonto()));
            preferenciaMP.setPlanNombre(dto.getPlanNombre());
            preferenciaMP.setPlanDias(dto.getPlanDias());
            preferenciaMP.setMembresiaId(dto.getMembresiaId()); // ✅ Guardar membresía
            preferenciaMP.setEmailCliente(dto.getEmailCliente());
            preferenciaMP.setEstado("CREADA");

            preferenciaMPRepository.save(preferenciaMP);
            log.info("💾 Preferencia persistida: preferenceId={}, clienteId={}, membresiaId={}", 
                    preference.getId(), dto.getClienteId(), dto.getMembresiaId());

            // ✅ Devolver respuesta simplificada (poblar campos que podían quedar null)
            return PagoResponseDto.builder()
                    .preferenceId(preference.getId())
                    .initPoint(initPoint)
                    .status("CREADA")
                    .message("Preferencia creada correctamente")
                    .qrCodeBase64(qrBase64)
                    .build();

        } catch (MPApiException apiEx) {
            System.err.println("⚠️ Error MercadoPago API: " + apiEx.getApiResponse().getContent());
            throw new Exception("Error en MercadoPago: " + apiEx.getMessage());
        } catch (MPException mpEx) {
            System.err.println("⚠️ Error SDK MercadoPago: " + mpEx.getMessage());
            throw new Exception("Error al conectar con MercadoPago");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error general al crear preferencia: " + e.getMessage());
        }
    }

    @Transactional
    public void procesarWebhook(String topic, String action, String paymentId, String body) {
        try {
            log.info("📩 Webhook recibido - topic: {}, action: {}, paymentId: {}", topic, action, paymentId);
            log.debug("Webhook body: {}", body);

            // Solo procesar si es un pago
            if (!"payment".equals(topic)) {
                log.warn("⚠️ Topic no es 'payment', ignorando: {}", topic);
                return;
            }

            // Buscar el pago pendiente más reciente por mpPaymentId
            List<Pago> pagosPendientes = pagoRepository.findAll().stream()
                    .filter(p -> "pendiente".equals(p.getEstado()))
                    .sorted((p1, p2) -> p2.getFechaRegistro().compareTo(p1.getFechaRegistro()))
                    .toList();

            if (pagosPendientes.isEmpty()) {
                log.warn("⚠️ No se encontraron pagos pendientes para procesar");
                return;
            }

            Pago pago = pagosPendientes.get(0);
            log.info("✅ Pago encontrado: ID={}, Cliente={}, MembresiaId={}", 
                    pago.getId(), 
                    pago.getCliente().getId(),
                    pago.getMembresia() != null ? pago.getMembresia().getId() : "NULL");

            // Actualizar el pago con el ID de MercadoPago
            if (pago.getMpPaymentId() == null) {
                pago.setMpPaymentId(paymentId);
            }

            pago.setEstado("aprobado");
            pago.setMpPayload(body);
            pagoRepository.save(pago);
            log.info("💾 Pago actualizado a estado 'aprobado'");

            // Actualizar membresía del cliente
            Long membresiaId = pago.getMembresia() != null ? pago.getMembresia().getId() : null;
            log.info("🔄 Actualizando membresía para cliente: {}, días: {}, membresiaId: {}", 
                    pago.getCliente().getId(), pago.getPlanDias(), membresiaId);
            
            actualizarMembresiaCliente(pago.getCliente(), pago.getPlanDias(), membresiaId, 
                    pago.getMontoFinal(), "MercadoPago");
            
            log.info("✅ Webhook procesado exitosamente - Cliente {} ahora tiene membresía activa", 
                    pago.getCliente().getId());

        } catch (Exception e) {
            log.error("❌ Error procesando webhook: {}", e.getMessage(), e);
        }
    }

    /**
     * Genera un PNG de QR a partir del texto (por ejemplo, initPoint) y devuelve los bytes.
     * Retorna null si no fue posible generarlo.
     */
    public byte[] generarQrPng(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int width = 250;
            int height = 250;
            BitMatrix matrix = new MultiFormatWriter().encode(texto, BarcodeFormat.QR_CODE, width, height);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            javax.imageio.ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("Error generando QR PNG: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Busca una preferencia por su preferenceId.
     * Lanza excepción si no se encuentra.
     */
    public PreferenciaMP obtenerPreferenciaPorId(String preferenceId) throws Exception {
        return preferenciaMPRepository.findByPreferenceId(preferenceId)
                .orElseThrow(() -> new Exception("Preferencia no encontrada: " + preferenceId));
    }

    /**
     * Genera el link de WhatsApp preformateado para compartir el pago.
     * Formato: https://api.whatsapp.com/send?text={mensaje_codificado}
     */
    public String generarWhatsAppLink(PreferenciaMP preferencia) {
        Cliente cliente = preferencia.getCliente();
        String mensaje = String.format(
            "Hola %s, para pagar tu suscripción %s por S/ %.2f usa este link: %s",
            cliente.getNombreCompleto(),
            preferencia.getPlanNombre() != null ? preferencia.getPlanNombre() : "al gimnasio",
            preferencia.getMonto(),
            preferencia.getInitPoint()
        );
        
        // URL encode del mensaje
        String mensajeCodificado = java.net.URLEncoder.encode(mensaje, java.nio.charset.StandardCharsets.UTF_8);
        return "https://api.whatsapp.com/send?text=" + mensajeCodificado;
    }

    /**
     * Crea un pago con Yape usando el token generado en el frontend.
     * 
     * @param dto Datos del pago con Yape (incluye token, clienteId, monto, etc.)
     * @return Información del pago creado
     * @throws Exception Si hay error al crear el pago o el pago es rechazado
     */
    @Transactional
    public PagoResponseDto crearPagoConYape(PagoYapeRequestDto dto) throws Exception {
        try {
            log.info("📥 crearPagoConYape payload: {}", dto);
            
            // 🔐 Configurar credencial
            MercadoPagoConfig.setAccessToken(mercadoPagoToken);

            // Buscar cliente
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + dto.getClienteId()));

            // 💳 Crear request de pago con Yape
            PaymentPayerRequest payer = PaymentPayerRequest.builder()
                    .email(dto.getEmailCliente() != null ? dto.getEmailCliente() : cliente.getEmail())
                    .build();

            String descripcion = dto.getDescripcion();
            if (descripcion == null || descripcion.isBlank()) {
                descripcion = "Membresía " + (dto.getPlanNombre() != null ? dto.getPlanNombre() : "Gimnasio VIP Center Fit");
            }

            PaymentCreateRequest paymentRequest = PaymentCreateRequest.builder()
                    .token(dto.getToken())
                    .transactionAmount(new BigDecimal(dto.getMonto()))
                    .description(descripcion)
                    .installments(1) // Yape siempre es 1 cuota (débito)
                    .paymentMethodId("yape")
                    .payer(payer)
                    .build();

            // 🚀 Crear pago en MercadoPago
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.create(paymentRequest);

            log.info("💰 Pago Yape creado: id={}, status={}", payment.getId(), payment.getStatus());

            // 💾 Guardar pago en BD
            Pago pago = new Pago();
            pago.setCliente(cliente);
            pago.setPlanNombre(dto.getPlanNombre());
            pago.setPlanDias(dto.getPlanDias());
            pago.setMontoFinal(new BigDecimal(dto.getMonto()));
            pago.setEstado(payment.getStatus()); // approved, rejected, pending, etc.
            pago.setMpPaymentId(payment.getId().toString());
            pagoRepository.save(pago);

            // ✅ Si el pago fue aprobado, actualizar membresía del cliente
            if ("approved".equals(payment.getStatus())) {
                actualizarMembresiaCliente(cliente, dto.getPlanDias(), dto.getMembresiaId(), 
                        new BigDecimal(dto.getMonto()), "Yape");
                log.info("✅ Membresía actualizada para cliente: {}", cliente.getId());
                
                // 📧 Enviar notificación de pago aprobado (email + SMS)
                notificationService.notificarPagoAprobado(cliente, pago);
            }

            // 📤 Devolver respuesta
            return PagoResponseDto.builder()
                    .preferenceId(payment.getId().toString()) // En pagos directos, el ID del payment
                    .initPoint(null) // No hay initPoint en pagos directos
                    .status(payment.getStatus())
                    .message(obtenerMensajePorEstado(payment.getStatus(), payment.getStatusDetail()))
                    .qrCodeBase64(null) // No aplica para Yape directo
                    .build();

        } catch (MPApiException apiEx) {
            log.error("⚠️ Error MercadoPago API: {}", apiEx.getApiResponse().getContent());
            throw new Exception("Error en MercadoPago: " + apiEx.getMessage());
        } catch (MPException mpEx) {
            log.error("⚠️ Error SDK MercadoPago: {}", mpEx.getMessage());
            throw new Exception("Error al conectar con MercadoPago");
        } catch (Exception e) {
            log.error("❌ Error general al crear pago con Yape: {}", e.getMessage(), e);
            throw new Exception("Error general al crear pago: " + e.getMessage());
        }
    }

    /**
     * Actualiza la fecha de vencimiento del cliente sumando días del plan.
     * Asigna la membresía al cliente.
     * Registra la renovación en la tabla de historial.
     * Si el cliente no tiene QR de acceso, se genera uno usando QRService.
     */
    protected void actualizarMembresiaCliente(Cliente cliente, Integer dias, Long membresiaId) {
        actualizarMembresiaCliente(cliente, dias, membresiaId, null, null);
    }

    /**
     * Actualiza la fecha de vencimiento del cliente sumando días del plan.
     * Asigna la membresía al cliente.
     * Registra la renovación en la tabla de historial.
     */
    protected void actualizarMembresiaCliente(Cliente cliente, Integer dias, Long membresiaId, 
                                             BigDecimal monto, String metodoPago) {
        if (dias == null || dias <= 0) return;

        // 📝 Guardar fecha anterior para historial
        LocalDate fechaAnterior = cliente.getFechaVencimiento();

        // ✅ Asignar membresía al cliente
        com.gimnasio.fit.entity.Membresia membresia = null;
        if (membresiaId != null) {
            membresia = membresiaRepository.findById(membresiaId).orElse(null);
            if (membresia != null) {
                cliente.setMembresiaActual(membresia);
                log.info("✅ Membresía asignada al cliente {}: {} (ID: {})", 
                        cliente.getId(), membresia.getNombre(), membresiaId);
            } else {
                log.warn("⚠️ No se encontró membresía con ID: {}", membresiaId);
            }
        }

        // ✅ Calcular nueva fecha de vencimiento
        LocalDate hoy = LocalDate.now();
        LocalDate base = (cliente.getFechaVencimiento() != null && cliente.getFechaVencimiento().isAfter(hoy))
                ? cliente.getFechaVencimiento()
                : hoy;

        LocalDate nuevaFecha = base.plusDays(dias);
        cliente.setFechaVencimiento(nuevaFecha);
        log.info("✅ Nueva fecha de vencimiento para cliente {}: {}", 
                cliente.getId(), nuevaFecha);

        // 💾 GUARDAR CLIENTE CON TODOS LOS CAMBIOS (membresía + fecha)
        clienteRepository.save(cliente);
        log.info("💾 Cliente actualizado y guardado en BD - ID: {}, Membresía ID: {}", 
                cliente.getId(), 
                cliente.getMembresiaActual() != null ? cliente.getMembresiaActual().getId() : "null");

        // 📊 REGISTRAR RENOVACIÓN EN HISTORIAL
        if (membresia != null) {
            com.gimnasio.fit.entity.Renovacion renovacion = new com.gimnasio.fit.entity.Renovacion();
            renovacion.setCliente(cliente);
            renovacion.setMembresia(membresia);
            renovacion.setDiasAgregados(dias);
            renovacion.setFechaVencimientoAnterior(fechaAnterior);
            renovacion.setFechaVencimientoNueva(nuevaFecha);
            renovacion.setMontoPagado(monto);
            renovacion.setMetodoPago(metodoPago);
            
            renovacionRepository.save(renovacion);
            log.info("✅ Renovación registrada en historial - Cliente: {}, Membresía: {}, Días: {}", 
                    cliente.getId(), membresia.getNombre(), dias);
        }
    }

    /**
     * Devuelve mensaje descriptivo según el estado del pago.
     */
    private String obtenerMensajePorEstado(String status, String statusDetail) {
        return switch (status) {
            case "approved" -> "Pago aprobado exitosamente";
            case "rejected" -> "Pago rechazado: " + traducirStatusDetail(statusDetail);
            case "pending" -> "Pago pendiente de confirmación";
            case "in_process" -> "Pago en proceso";
            case "cancelled" -> "Pago cancelado";
            default -> "Estado del pago: " + status;
        };
    }

    /**
     * Traduce el status_detail de MercadoPago a mensajes legibles.
     */
    private String traducirStatusDetail(String detail) {
        if (detail == null) return "motivo desconocido";
        return switch (detail) {
            case "cc_rejected_insufficient_amount" -> "Saldo insuficiente";
            case "cc_rejected_bad_filled_security_code" -> "Código OTP incorrecto";
            case "cc_rejected_call_for_authorize" -> "Debes autorizar el pago en tu banco";
            case "cc_rejected_card_type_not_allowed" -> "Método de pago no permitido";
            case "cc_rejected_max_attempts" -> "Excediste el número de intentos permitidos";
            case "cc_rejected_other_reason" -> "Pago rechazado por el banco";
            default -> detail;
        };
    }

    /**
     * Procesa un reembolso de pago.
     * 
     * @param pagoId ID del pago a reembolsar
     * @param motivo Motivo del reembolso
     * @param monto Monto a reembolsar (null = reembolso total)
     * @return Información del reembolso procesado
     * @throws Exception Si hay error al procesar el reembolso
     */
    @Transactional
    public com.gimnasio.fit.dto.ReembolsoResponseDto procesarReembolso(Long pagoId, String motivo, Double monto) throws Exception {
        try {
            log.info("💰 Procesando reembolso para pago: {}", pagoId);

            // Buscar pago
            Pago pago = pagoRepository.findById(pagoId)
                    .orElseThrow(() -> new Exception("Pago no encontrado con ID: " + pagoId));

            // Validar que el pago esté aprobado
            if (!"aprobado".equals(pago.getEstado()) && !"approved".equals(pago.getEstado())) {
                throw new Exception("Solo se pueden reembolsar pagos aprobados. Estado actual: " + pago.getEstado());
            }

            // Validar que tenga payment ID de MercadoPago
            if (pago.getMpPaymentId() == null || pago.getMpPaymentId().isBlank()) {
                throw new Exception("El pago no tiene ID de MercadoPago asociado");
            }

            // Configurar credencial
            MercadoPagoConfig.setAccessToken(mercadoPagoToken);

            // Determinar monto a reembolsar
            BigDecimal montoReembolso = monto != null 
                    ? new BigDecimal(monto) 
                    : pago.getMontoFinal();

            // Validar que no exceda el monto del pago
            if (montoReembolso.compareTo(pago.getMontoFinal()) > 0) {
                throw new Exception("El monto a reembolsar no puede ser mayor al monto del pago");
            }

            // Crear reembolso en MercadoPago
            com.mercadopago.client.payment.PaymentRefundClient refundClient = 
                    new com.mercadopago.client.payment.PaymentRefundClient();
            
            com.mercadopago.resources.payment.PaymentRefund refund = 
                    refundClient.refund(Long.parseLong(pago.getMpPaymentId()), montoReembolso);

            log.info("💰 Reembolso creado: id={}, status={}, amount={}", 
                    refund.getId(), refund.getStatus(), refund.getAmount());

            // Actualizar estado del pago en BD
            pago.setEstado("reembolsado");
            pago.setMpPayload(pago.getMpPayload() + " | REEMBOLSO: " + motivo);
            pagoRepository.save(pago);

            // Ajustar membresía del cliente (restar días si fue reembolso total)
            if (montoReembolso.compareTo(pago.getMontoFinal()) == 0) {
                ajustarMembresiaPostReembolso(pago.getCliente(), pago.getPlanDias());
            }

            // 📧 Enviar notificación de reembolso
            notificationService.notificarReembolso(pago.getCliente(), pago, montoReembolso);

            // Retornar respuesta
            return com.gimnasio.fit.dto.ReembolsoResponseDto.builder()
                    .refundId(refund.getId().toString())
                    .paymentId(pago.getMpPaymentId())
                    .montoReembolsado(montoReembolso.doubleValue())
                    .status(refund.getStatus())
                    .message("Reembolso procesado exitosamente")
                    .fechaEstimada("5-10 días hábiles")
                    .build();

        } catch (MPApiException apiEx) {
            log.error("⚠️ Error MercadoPago API al crear reembolso: {}", apiEx.getApiResponse().getContent());
            throw new Exception("Error en MercadoPago: " + apiEx.getMessage());
        } catch (MPException mpEx) {
            log.error("⚠️ Error SDK MercadoPago al crear reembolso: {}", mpEx.getMessage());
            throw new Exception("Error al conectar con MercadoPago");
        } catch (Exception e) {
            log.error("❌ Error general al procesar reembolso: {}", e.getMessage(), e);
            throw new Exception("Error al procesar reembolso: " + e.getMessage());
        }
    }

    /**
     * Ajusta la membresía del cliente restando días tras un reembolso total.
     */
    private void ajustarMembresiaPostReembolso(Cliente cliente, Integer dias) {
        if (dias == null || dias <= 0) return;

        LocalDate fechaActual = cliente.getFechaVencimiento();
        if (fechaActual != null) {
            // Restar días de la membresía
            LocalDate nuevaFecha = fechaActual.minusDays(dias);
            
            // Si la nueva fecha es anterior a hoy, marcar como vencido
            if (nuevaFecha.isBefore(LocalDate.now())) {
                nuevaFecha = null; // Sin membresía
            }
            
            cliente.setFechaVencimiento(nuevaFecha);
            clienteRepository.save(cliente);
            log.info("🔄 Membresía ajustada post-reembolso para cliente: {}", cliente.getId());
        }
    }

    /**
     * Verifica un pago manual (Yape QR, Efectivo, Transferencia) y asigna la membresía.
     * Este método se llama cuando el administrador verifica manualmente que el pago fue realizado.
     */
    @Transactional
    public PagoResponseDto verificarPagoManual(Long clienteId, Long membresiaId, Integer planDias, 
                                               Double monto, String planNombre) throws Exception {
        try {
            log.info("📥 Verificar pago manual: clienteId={}, membresiaId={}, días={}, monto={}", 
                    clienteId, membresiaId, planDias, monto);

            // Buscar cliente
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));

            // � Obtener usuario que registra el pago
            com.gimnasio.fit.entity.Usuario usuarioRegistrador = obtenerUsuarioAutenticado();
            if (usuarioRegistrador != null && cliente.getRegistradoPor() == null) {
                cliente.setRegistradoPor(usuarioRegistrador);
                log.info("👤 Usuario registrador asignado: {} ({})", 
                        usuarioRegistrador.getNombre(), usuarioRegistrador.getEmail());
            }

            // �💾 Crear registro de pago en BD
            Pago pago = new Pago();
            pago.setCliente(cliente);
            pago.setPlanNombre(planNombre != null ? planNombre : "Membresía Gimnasio");
            pago.setPlanDias(planDias);
            pago.setMontoFinal(monto != null ? new BigDecimal(monto) : BigDecimal.ZERO);
            pago.setEstado("aprobado"); // Verificado manualmente
            pago.setMetodoPago("Manual"); // 🔥 Método de pago manual (Yape/Efectivo/Transferencia)
            pago.setMpPaymentId("MANUAL-" + System.currentTimeMillis()); // ID único para pago manual
            
            // Asignar membresía si se proporciona
            if (membresiaId != null) {
                com.gimnasio.fit.entity.Membresia membresia = membresiaRepository.findById(membresiaId)
                        .orElse(null);
                if (membresia != null) {
                    pago.setMembresia(membresia);
                }
            }
            
            pagoRepository.save(pago);
            log.info("💾 Pago manual guardado: ID={}", pago.getId());

            // ✅ Actualizar membresía del cliente y registrar renovación
            actualizarMembresiaCliente(cliente, planDias, membresiaId, 
                    monto != null ? new BigDecimal(monto) : BigDecimal.ZERO, "Manual");
            log.info("✅ Membresía actualizada manualmente para cliente: {}", cliente.getId());

            // 📤 Devolver respuesta
            return PagoResponseDto.builder()
                    .preferenceId(pago.getId().toString())
                    .status("approved")
                    .message("Pago verificado manualmente - Membresía asignada exitosamente")
                    .qrCodeBase64(cliente.getQrAcceso()) // Devolver QR del cliente
                    .build();

        } catch (Exception e) {
            log.error("❌ Error al verificar pago manual: {}", e.getMessage(), e);
            throw new Exception("Error al verificar pago manual: " + e.getMessage());
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado desde el contexto de seguridad.
     */
    private com.gimnasio.fit.entity.Usuario obtenerUsuarioAutenticado() {
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
}
