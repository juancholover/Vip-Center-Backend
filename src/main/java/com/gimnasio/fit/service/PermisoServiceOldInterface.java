package com.gimnasio.fit.service;

import com.gimnasio.fit.entity.Permiso;

import java.util.List;

/**
 * Interfaz de servicio para gestión de permisos (antigua)
 */
public interface PermisoServiceOldInterface {
    Permiso crear(Permiso p);
    List<Permiso> listar();
}
