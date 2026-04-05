package com.gimnasio.fit.service;

import com.gimnasio.fit.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario crear(Usuario u);
    List<Usuario> listar();
    Optional<Usuario> buscarPorEmail(String email);
    void asignarRol(Integer usuarioId, Integer rolId, Integer asignadoPor);
}