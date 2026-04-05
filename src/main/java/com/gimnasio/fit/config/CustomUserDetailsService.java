package com.gimnasio.fit.config;

import com.gimnasio.fit.entity.Rol;
import com.gimnasio.fit.entity.Usuario;
import com.gimnasio.fit.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.fetchWithRolesAndPermisos(username)
                .orElseThrow(() -> new UsernameNotFoundException("❌ No existe usuario con email: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();

        System.out.println("========== LOGIN DETECTADO ==========");
        System.out.println("👤 Usuario: " + u.getEmail());
        System.out.println("🟢 Activo: " + u.getActivo());
        System.out.println("🔐 Roles asignados:");

        // Roles
        u.getUsuarioRoles().forEach(ur -> {
            Rol rol = ur.getRol();
            String roleName = "ROLE_" + rol.getNombre().toUpperCase();
            authorities.add(new SimpleGrantedAuthority(roleName));
            System.out.println("   🧩 Rol detectado: " + roleName);

            // Permisos asociados
            if (rol.getRolPermisos() != null && !rol.getRolPermisos().isEmpty()) {
                System.out.println("   ├─ Permisos del rol '" + rol.getNombre() + "':");
                rol.getRolPermisos().forEach(rp -> {
                    if (rp.getPermiso() != null) {
                        String code = rp.getPermiso().getCodigo();
                        authorities.add(new SimpleGrantedAuthority(code));
                        System.out.println("   │   • " + code);
                    }
                });
                System.out.println("   └───────────────────────────────");
            }
        });

        System.out.println("====================================\n");

        return User.builder()
                .username(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(authorities)
                .disabled(!u.getActivo())
                .build();
    }
}
