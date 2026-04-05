package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.entity.Permiso;
import com.gimnasio.fit.repository.PermisoRepository;
import com.gimnasio.fit.service.PermisoServiceOldInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("permisoServiceOld")
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoServiceOldInterface {

    private final PermisoRepository permisoRepository;

    @Override
    public Permiso crear(Permiso p) {
        if (permisoRepository.existsByModuloAndAccion(p.getModulo(), p.getAccion())) {
            throw new IllegalArgumentException("Ya existe permiso para modulo+accion");
        }
        if (p.getCodigo() == null || p.getCodigo().isBlank()) {
            p.setCodigo(p.getModulo() + "." + p.getAccion());
        }
        return permisoRepository.save(p);
    }

    @Override
    public List<Permiso> listar() {
        return permisoRepository.findAll();
    }
}