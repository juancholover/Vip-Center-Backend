package com.gimnasio.fit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para actualizar datos de un cliente.
 * 
 * Notas:
 * - Todos los campos son opcionales (solo se actualizan los proporcionados)
 * - NO se puede modificar: qr_acceso, fecha_vencimiento (manejados por el sistema)
 */
@Getter
@Setter
public class ActualizarClienteRequest {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
}
