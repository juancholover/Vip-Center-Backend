package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Clientes del gimnasio (NO son usuarios del sistema).
 * 
 * Estado calculado dinámicamente:
 * - Activo: fecha_vencimiento >= HOY AND qr_activo = true
 * - Vencido: fecha_vencimiento < HOY
 * - Sin membresía: fecha_vencimiento IS NULL
 * - QR deshabilitado: qr_activo = false
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "clientes",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_cliente_unico", 
                           columnNames = {"nombre", "apellido", "telefono"})
       },
       indexes = {
           @Index(name = "idx_telefono", columnList = "telefono"),
           @Index(name = "idx_qr_acceso", columnList = "qr_acceso"),
           @Index(name = "idx_fecha_vencimiento", columnList = "fecha_vencimiento"),
           @Index(name = "idx_qr_estado", columnList = "qr_acceso, qr_activo")
       })
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, nullable = false)
    private String apellido;

    @Column(length = 20, nullable = false)
    private String telefono;

    @Column(length = 100)
    private String email;

    /**
     * Documento Nacional de Identidad del cliente.
     * Opcional — usado para emitir boletas.
     */
    @Column(length = 20)
    private String dni;

    /**
     * Código QR único para acceso al gimnasio.
     * - Se genera SOLO en el primer pago confirmado
     * - NULL hasta que se realice el primer pago
     * - Soporta UUID v4 (36 chars) o SHA256 base64 (44 chars)
     */
    @Column(name = "qr_acceso", length = 64, unique = true)
    private String qrAcceso;

    /**
     * Indica si el QR está activo.
     * - false si fue reportado perdido/robado
     * - Al regenerar QR, el anterior se marca como inactivo
     */
    @Column(name = "qr_activo", nullable = false)
    private Boolean qrActivo = true;

    /**
     * Fecha hasta la cual la membresía está vigente.
     * - Se actualiza automáticamente al confirmar pago
     * - NUNCA actualizar manualmente sin crear registro en pagos
     * - NULL = sin membresía activa
     */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    /**
     * Membresía actual del cliente.
     * - Se actualiza al confirmar un pago
     * - NULL = sin membresía activa
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membresia_id")
    private Membresia membresiaActual;

    /**
     * Usuario del sistema que registró a este cliente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;

    /**
     * Fecha y hora en que se registró al cliente.
     * Se establece automáticamente por la base de datos al insertar.
     */
    @Column(name = "fecha_registro", updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private Instant fechaRegistro;

    /**
     * Estado de seguimiento para la bandeja de recepción (HU-32).
     * Valores: "PENDIENTE", "LLAMADO", "PROMESA"
     * Usado por recepcionistas para trackear el contacto con clientes próximos a vencer.
     */
    @Column(name = "estado_seguimiento", length = 20)
    private String estadoSeguimiento = "PENDIENTE";

    // ====================================
    // Métodos de conveniencia
    // ====================================

    /**
     * Calcula el estado actual del cliente basado en:
     * - fecha_vencimiento
     * - qr_activo
     * 
     * @return Estado: "activo", "vencido", "qr_deshabilitado"
     * NOTA: "sin_membresia" fue reemplazado por "vencido" (solicitado por el cliente)
     */
    @Transient
    public String getEstado() {
        if (fechaVencimiento == null) {
            return "vencido";
        }
        if (!qrActivo) {
            return "qr_deshabilitado";
        }
        LocalDate hoy = LocalDate.now();
        if (fechaVencimiento.isAfter(hoy) || fechaVencimiento.isEqual(hoy)) {
            return "activo";
        }
        return "vencido";
    }

    /**
     * Retorna el nombre completo del cliente.
     */
    @Transient
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Verifica si el cliente tiene membresía activa.
     */
    @Transient
    public boolean tieneMembresiaActiva() {
        return "activo".equals(getEstado());
    }
}
