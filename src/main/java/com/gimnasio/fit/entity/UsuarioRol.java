package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="usuario_roles",
       uniqueConstraints = @UniqueConstraint(name="uk_usuario_rol", columnNames = {"usuario_id","rol_id"}))
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="usuario_id", nullable=false)
    private Usuario usuario;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="rol_id", nullable=false)
    private Rol rol;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="asignado_por")
    private Usuario asignadoPor;

    @Column(name="fecha_asignacion", insertable=false, updatable=false)
    private Instant fechaAsignacion;
}