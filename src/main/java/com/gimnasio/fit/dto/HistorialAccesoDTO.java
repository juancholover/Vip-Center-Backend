package com.gimnasio.fit.dto;

import com.gimnasio.fit.entity.HistorialAcceso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialAccesoDTO {
    private Long id;
    private String username;
    private String tipoEvento;
    private String descripcionEvento;
    private String ipAddress;
    private String navegador; // Extraído del user agent
    private String sistemaOperativo; // Extraído del user agent
    private Boolean exitoso;
    private LocalDateTime fechaHora;
    private String detalles;
    
    // Constructor desde Entity
    public static HistorialAccesoDTO fromEntity(HistorialAcceso entity) {
        HistorialAccesoDTO dto = new HistorialAccesoDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setTipoEvento(entity.getTipoEvento().name());
        dto.setDescripcionEvento(entity.getTipoEvento().getDescripcion());
        dto.setIpAddress(entity.getIpAddress());
        dto.setExitoso(entity.getExitoso());
        dto.setFechaHora(entity.getFechaHora());
        dto.setDetalles(entity.getDetalles());
        
        // Extraer info del user agent
        if (entity.getUserAgent() != null) {
            String[] info = extraerInfoUserAgent(entity.getUserAgent());
            dto.setNavegador(info[0]);
            dto.setSistemaOperativo(info[1]);
        }
        
        return dto;
    }
    
    private static String[] extraerInfoUserAgent(String userAgent) {
        String navegador = "Desconocido";
        String so = "Desconocido";
        
        if (userAgent == null) return new String[]{navegador, so};
        
        String ua = userAgent.toLowerCase();
        
        // Detectar navegador
        if (ua.contains("edg/")) navegador = "Edge";
        else if (ua.contains("chrome/")) navegador = "Chrome";
        else if (ua.contains("firefox/")) navegador = "Firefox";
        else if (ua.contains("safari/") && !ua.contains("chrome")) navegador = "Safari";
        else if (ua.contains("opera/") || ua.contains("opr/")) navegador = "Opera";
        
        // Detectar SO
        if (ua.contains("windows")) so = "Windows";
        else if (ua.contains("mac os")) so = "macOS";
        else if (ua.contains("linux")) so = "Linux";
        else if (ua.contains("android")) so = "Android";
        else if (ua.contains("iphone") || ua.contains("ipad")) so = "iOS";
        
        return new String[]{navegador, so};
    }
}
