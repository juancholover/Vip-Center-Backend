package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.entity.*;
import com.gimnasio.fit.repository.*;
import com.gimnasio.fit.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("usuarioServiceOld")
@RequiredArgsConstructor
public class UsuarioServiceImplOld implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario crear(Usuario u) {
        if (usuarioRepository.existsByEmail(u.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        u.setPasswordHash(passwordEncoder.encode(u.getPasswordHash()));
        return usuarioRepository.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllWithRoles();
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void asignarRol(Integer usuarioId, Integer rolId, Integer asignadoPor) {
        Usuario usuario = usuarioRepository.findById(usuarioId.longValue()) // ✅ Convertir a Long
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Rol rol = rolRepository.findById(rolId.longValue()) // ✅ Convertir a Long
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado"));

        boolean yaExiste = usuario.getUsuarioRoles().stream()
                .anyMatch(ur -> ur.getRol().getId().equals(rolId.longValue())); // ✅ Convertir a Long
        if (yaExiste) {
            return;
        }

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuario);
        ur.setRol(rol);
        if (asignadoPor != null) {
            usuarioRepository.findById(asignadoPor.longValue()).ifPresent(ur::setAsignadoPor); // ✅ Convertir a Long
        }
        usuario.getUsuarioRoles().add(ur);
        usuarioRolRepository.save(ur);
    }
}