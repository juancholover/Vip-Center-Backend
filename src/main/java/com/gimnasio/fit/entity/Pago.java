package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity @Table(name = "pagos", indexes = {
        @Index(name="idx_pago_cliente", columnList="cliente_id"),
        @Index(name="idx_pago_mp", columnList="mp_preference_id, mp_payment_id")
})
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * Membresía asociada a este pago.
     * - Se registra al momento de crear el pago
     * - Si es NULL, usar planNombre y planDias (compatibilidad con pagos antiguos)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membresia_id")
    private Membresia membresia;

    @Column(nullable = false, length = 80)
    private String planNombre;       // "Mensual", "3 Meses", etc.

    @Column(nullable = false)
    private Integer planDias;        // 30, 90, 180, 365

    @Column(nullable = false, scale = 2, precision = 12)
    private BigDecimal montoFinal;

    @Column(nullable = false, length = 20)
    private String estado;           // "pendiente", "aprobado", "rechazado"

    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;       // "Efectivo", "Tarjeta", "Transferencia", "MercadoPago", "Yape"

    @Column(name = "mp_preference_id", length = 64)
    private String mpPreferenceId;

    @Column(name = "mp_payment_id", length = 64)
    private String mpPaymentId;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro = Instant.now();

    @Lob
    private String mpPayload; // JSON del webhook (opcional)
}
