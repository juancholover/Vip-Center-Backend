package com.gimnasio.fit.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Data
public class PagoRequestDto {
    @NotNull(message = "clienteId es requerido")
    private Long clienteId;        // 🔹 ID del cliente

    @NotNull(message = "monto es requerido")
    private Double monto;          // 🔹 Monto a pagar

    private String descripcion;    // 🔹 Descripción o concepto
    private String metodo;         // 🔹 Método de pago (YAPE, EFECTIVO, etc.)
    private String planNombre;     // 🔹 Nombre del plan
    private Integer planDias;      // 🔹 Días de duración del plan
    
    private Long membresiaId;      // 🔹 ID de la membresía seleccionada

    @Email(message = "emailCliente debe ser un correo válido")
    private String emailCliente;   // 🔹 Email del cliente
}
