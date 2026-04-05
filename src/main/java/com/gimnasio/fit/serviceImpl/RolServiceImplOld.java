package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.entity.Rol;
import com.gimnasio.fit.repository.RolRepository;
import com.gimnasio.fit.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("rolServiceOld")
@RequiredArgsConstructor
public class RolServiceImplOld implements RolService {

    private final RolRepository rolRepository;

    @Override
    public Rol crear(Rol r) {
        return rolRepository.save(r);
    }

    @Override
    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    @Override
    public Optional<Rol> buscarPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }
}