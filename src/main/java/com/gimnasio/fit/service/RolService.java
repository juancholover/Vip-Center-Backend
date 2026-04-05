package com.gimnasio.fit.service;

import com.gimnasio.fit.entity.Rol;

import java.util.List;
import java.util.Optional;

public interface RolService {
    Rol crear(Rol r);
    List<Rol> listar();
    Optional<Rol> buscarPorNombre(String nombre);
}