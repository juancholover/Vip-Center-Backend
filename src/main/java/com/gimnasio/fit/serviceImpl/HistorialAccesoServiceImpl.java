package com.gimnasio.fit.serviceImpl;

import com.gimnasio.fit.dto.HistorialAccesoDTO;
import com.gimnasio.fit.entity.HistorialAcceso;
import com.gimnasio.fit.repository.HistorialAccesoRepository;
import com.gimnasio.fit.service.HistorialAccesoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistorialAccesoServiceImpl implements HistorialAccesoService {
    
    private final HistorialAccesoRepository historialRepository;
    
    @Override
    @Transactional
    public HistorialAcceso registrarEvento(
            Long usuarioId,
            String username,
            HistorialAcceso.TipoEvento tipoEvento,
            String ipAddress,
            String userAgent,
            Boolean exitoso,
            String detalles
    ) {
        try {
            HistorialAcceso historial = new HistorialAcceso();
            historial.setUsuarioId(usuarioId);
            historial.setUsername(username);
            historial.setTipoEvento(tipoEvento);
            historial.setIpAddress(ipAddress);
            historial.setUserAgent(userAgent);
            historial.setExitoso(exitoso);
            historial.setDetalles(detalles);
            historial.setFechaHora(LocalDateTime.now());
            
            HistorialAcceso saved = historialRepository.save(historial);
            log.info("Evento registrado: {} - Usuario: {} - IP: {}", 
                tipoEvento, username, ipAddress);
            return saved;
        } catch (Exception e) {
            log.error("Error al registrar evento en historial: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public void registrarLoginExitoso(Long usuarioId, String username, String ip, String userAgent) {
        registrarEvento(usuarioId, username, HistorialAcceso.TipoEvento.LOGIN, 
            ip, userAgent, true, null);
    }
    
    @Override
    @Transactional
    public void registrarLoginFallido(String username, String ip, String userAgent, String motivo) {
        registrarEvento(null, username, HistorialAcceso.TipoEvento.LOGIN_FAILED, 
            ip, userAgent, false, motivo);
    }
    
    @Override
    @Transactional
    public void registrarLogout(Long usuarioId, String username, String ip, String userAgent) {
        registrarEvento(usuarioId, username, HistorialAcceso.TipoEvento.LOGOUT, 
            ip, userAgent, true, null);
    }
    
    @Override
    @Transactional
    public void registrarCambioPassword(Long usuarioId, String username, String ip, String userAgent) {
        registrarEvento(usuarioId, username, HistorialAcceso.TipoEvento.PASSWORD_CHANGE, 
            ip, userAgent, true, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<HistorialAccesoDTO> obtenerHistorialUsuario(Long usuarioId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("fechaHora").descending());
        return historialRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId, pageRequest)
            .map(HistorialAccesoDTO::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<HistorialAccesoDTO> obtenerHistorialPorUsername(String username, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("fechaHora").descending());
        return historialRepository.findByUsernameOrderByFechaHoraDesc(username, pageRequest)
            .map(HistorialAccesoDTO::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialAccesoDTO> obtenerUltimosAccesos(Long usuarioId, int limite) {
        PageRequest pageRequest = PageRequest.of(0, limite, Sort.by("fechaHora").descending());
        return historialRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId, pageRequest)
            .getContent()
            .stream()
            .map(HistorialAccesoDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialAccesoDTO> obtenerIntentosfallidosRecientes(int horas) {
        LocalDateTime desde = LocalDateTime.now().minusHours(horas);
        return historialRepository.findIntentosFallidos(desde)
            .stream()
            .map(HistorialAccesoDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneMuchosIntentosFallidos(String username, int maxIntentos, int minutosVentana) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(minutosVentana);
        long intentos = historialRepository.countIntentosfallidosRecientes(username, desde);
        return intentos >= maxIntentos;
    }
    
    @Override
    @Transactional(readOnly = true)
    public HistorialAccesoDTO obtenerUltimoAccesoExitoso(Long usuarioId) {
        List<HistorialAcceso> accesos = historialRepository.findUltimoAccesoExitoso(usuarioId);
        if (accesos.isEmpty()) {
            return null;
        }
        return HistorialAccesoDTO.fromEntity(accesos.get(0));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialAccesoDTO> obtenerHistorialPorFechas(
            Long usuarioId, 
            LocalDateTime inicio, 
            LocalDateTime fin
    ) {
        return historialRepository.findByUsuarioAndFechaRange(usuarioId, inicio, fin)
            .stream()
            .map(HistorialAccesoDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
