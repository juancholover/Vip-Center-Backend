package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="permisos",
       uniqueConstraints = {
         @UniqueConstraint(name="uk_codigo", columnNames = {"codigo"}),
         @UniqueConstraint(name="uk_mod_accion", columnNames={"modulo","accion"})
       })
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=50, nullable=false)
    private String codigo; // Ej: clientes.crear

    @Column(length=50, nullable=false)
    private String modulo;

    @Column(length=50, nullable=false)
    private String accion;

    @Column
    private String descripcion;


}