package com.gimnasio.fit.config;

import com.gimnasio.fit.entity.*;
import com.gimnasio.fit.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Inicializa datos base: permisos, rol admin, usuario admin, membresías.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final MembresiaRepository membresiaRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Crear permisos base
        Map<String, Permiso> permisosMap = crearPermisosBase();

        // 2. Crear rol admin (si no existe)
        Rol rolAdmin = rolRepository.findByNombre("admin").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("admin");
            r.setDescripcion("Acceso total");
            r.setActivo(true);
            return rolRepository.save(r);
        });

        // 3. Asociar todos los permisos al rol admin (si faltan)
        Set<Long> idsYaAsociados = rolAdmin.getRolPermisos().stream() // ✅ Cambiado a Long
                .map(rp -> rp.getPermiso().getId())
                .collect(Collectors.toSet());

        List<RolPermiso> nuevos = new ArrayList<>();
        for (Permiso p : permisosMap.values()) {
            if (!idsYaAsociados.contains(p.getId())) {
                RolPermiso rp = new RolPermiso();
                rp.setRol(rolAdmin);
                rp.setPermiso(p);
                nuevos.add(rp);
            }
        }
        if (!nuevos.isEmpty()) {
            rolPermisoRepository.saveAll(nuevos);
        }

        // 4. Crear usuario admin (si no existe) - DESDE VARIABLES DE ENTORNO
        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        
        // Valores por defecto SOLO para desarrollo local (NO usar en producción)
        if (adminEmail == null || adminEmail.isBlank()) {
            adminEmail = "admin@gym.com";
            System.out.println("⚠️ ADVERTENCIA: Usando email admin por defecto. Configure ADMIN_EMAIL en producción.");
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            adminPassword = "Admin123!";
            System.out.println("⚠️ ADVERTENCIA: Usando password admin por defecto. Configure ADMIN_PASSWORD en producción.");
        }
        
        final String emailFinal = adminEmail;
        final String passwordFinal = adminPassword;
        
        Usuario admin = usuarioRepository.findByEmail(emailFinal).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setEmail(emailFinal);
            u.setPasswordHash(passwordEncoder.encode(passwordFinal));
            u.setNombre("Admin");
            u.setApellido("Principal");
            u.setTelefono("000000000");
            u.setActivo(true);
            u.setDebeCambiarPassword(true); // 🔐 OBLIGAR cambio de contraseña en primer login
            return usuarioRepository.save(u);
        });

        // 5. Asignar rol admin a usuario admin si falta
        boolean yaTiene = admin.getUsuarioRoles().stream()
                .anyMatch(ur -> ur.getRol().getNombre().equalsIgnoreCase("admin"));

        if (!yaTiene) {
            UsuarioRol ur = new UsuarioRol();
            ur.setUsuario(admin);
            ur.setRol(rolAdmin);
            usuarioRolRepository.save(ur);
        }

        // 6. Crear membresías por defecto
        crearMembresiasIniciales();

        // Log simple en consola
        System.out.println("=== DataInitializer completado ===");
    }

    private void crearMembresiasIniciales() {
        // Verificar si ya existen membresías
        if (membresiaRepository.count() > 0) {
            System.out.println("✅ Membresías ya inicializadas");
            return;
        }

        System.out.println("📋 Creando membresías por defecto...");

        List<Membresia> membresias = Arrays.asList(
            Membresia.builder()
                .nombre("Membresía Mensual")
                .descripcion("Acceso completo al gimnasio por 30 días")
                .duracionDias(30)
                .precio(new java.math.BigDecimal("50.00"))
                .estado(true)
                .color("#3B82F6")
                .orden(1)
                .build(),

            Membresia.builder()
                .nombre("Membresía Trimestral")
                .descripcion("3 meses de acceso completo con descuento")
                .duracionDias(90)
                .precio(new java.math.BigDecimal("120.00"))
                .estado(true)
                .color("#10B981")
                .orden(2)
                .build(),

            Membresia.builder()
                .nombre("Membresía Semestral")
                .descripcion("6 meses de acceso con descuento")
                .duracionDias(180)
                .precio(new java.math.BigDecimal("200.00"))
                .estado(true)
                .color("#F59E0B")
                .orden(3)
                .build(),

            Membresia.builder()
                .nombre("Membresía Anual")
                .descripcion("12 meses de acceso - ¡Mejor oferta!")
                .duracionDias(365)
                .precio(new java.math.BigDecimal("350.00"))
                .estado(true)
                .color("#EF4444")
                .orden(4)
                .build(),

            Membresia.builder()
                .nombre("Pase Semanal")
                .descripcion("Perfecto para probar el gimnasio - 7 días")
                .duracionDias(7)
                .precio(new java.math.BigDecimal("20.00"))
                .estado(true)
                .color("#8B5CF6")
                .orden(0)
                .build()
        );

        membresiaRepository.saveAll(membresias);
        System.out.println("✅ " + membresias.size() + " membresías creadas exitosamente");
    }

    private Map<String, Permiso> crearPermisosBase() {
        // Lista basada en tu contexto
        String[] codigos = {
                "usuarios.crear",
                "usuarios.ver",
                "usuarios.editar",
                "usuarios.asignar_rol",
                "clientes.crear",
                "clientes.ver",
                "clientes.editar",
                "clientes.regenerar_qr",
                "pagos.crear",
                "pagos.ver_mes",
                "pagos.ver_todos",
                "pagos.confirmar",
                "membresias.crear",
                "membresias.editar",
                "asistencias.validar",
                "asistencias.ver",
                "reportes.ingresos",
                "reportes.asistencias",
                "reportes.clientes_activos"
        };

        Map<String, Permiso> result = new HashMap<>();
        for (String code : codigos) {
            Permiso p = permisoRepository.findByCodigo(code).orElseGet(() -> {
                String[] parts = code.split("\\.");
                String modulo = parts[0];
                String accion = parts.length > 1 ? parts[1] : "accion";
                
                Permiso nuevo = new Permiso();
                nuevo.setCodigo(code);
                nuevo.setModulo(modulo);
                nuevo.setAccion(accion);
                nuevo.setDescripcion("Permiso " + code);
                return permisoRepository.save(nuevo);
            });
            result.put(code, p);
        }
        return result;
    }
    
}