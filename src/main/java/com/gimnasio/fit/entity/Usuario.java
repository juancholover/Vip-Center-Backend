package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ Cambiado de Integer a Long

    @Column(length=100, nullable=false, unique=true)
    private String email;

    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;

    @Column(nullable=false, length=50)
    private String nombre;

    @Column(nullable=false, length=50)
    private String apellido;

    @Column(length=20)
    private String telefono;

    @Column(nullable=false)
    private Boolean activo = true;

    // Campos de seguridad
    @Column(name="debe_cambiar_password", nullable=false)
    private Boolean debeCambiarPassword = false;

    @Column(name="intentos_fallidos", nullable=false)
    private Integer intentosFallidos = 0;

    @Column(name="fecha_bloqueo")
    private Instant fechaBloqueo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="creado_por")
    private Usuario creadoPor;

    @Column(name="fecha_creacion", insertable=false, updatable=false)
    private Instant fechaCreacion;

    @Column(name="fecha_modificacion") // ✅ NUEVO
    private Instant fechaModificacion;

    @Column(name="ultimo_acceso")
    private Instant ultimoAcceso;

    // ✅ NUEVO: Relación directa ManyToMany con Rol
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private List<Rol> roles = new ArrayList<>();

    // Mantener relación antigua por compatibilidad (opcional)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRol> usuarioRoles = new HashSet<>();

    // ✅ Métodos helper para compatibilidad con código existente
    public String getPassword() {
        return this.passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }
}