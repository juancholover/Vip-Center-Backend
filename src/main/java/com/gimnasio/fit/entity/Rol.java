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
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ Cambiado a Long

    @Column(length=50, nullable=false, unique=true)
    private String nombre;

    @Column
    private String descripcion;

    @Column(nullable=false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="creado_por")
    private Usuario creadoPor;

    @Column(name="fecha_creacion", insertable=false, updatable=false)
    private Instant fechaCreacion;

    // Mantener relaciones antiguas por compatibilidad
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRol> usuarioRoles = new HashSet<>();

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RolPermiso> rolPermisos = new HashSet<>();

    // ✅ NUEVO: Relación directa ManyToMany con Usuario
    @ManyToMany(mappedBy = "roles")
    private List<Usuario> usuarios = new ArrayList<>();

    // ✅ NUEVO: Relación directa ManyToMany con Permiso
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rol_permisos",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private List<Permiso> permisos = new ArrayList<>();
}