package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="rol_permisos",
       uniqueConstraints = @UniqueConstraint(name="uk_rol_permiso", columnNames = {"rol_id","permiso_id"}))
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="rol_id", nullable=false)
    private Rol rol;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="permiso_id", nullable=false)
    private Permiso permiso;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="asignado_por")
    private Usuario asignadoPor;

    @Column(name="fecha_asignacion", insertable=false, updatable=false)
    private Instant fechaAsignacion;
}