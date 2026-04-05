package com.gimnasio.fit.entity;

/**
 * Tipos de registro de asistencia disponibles en el sistema.
 */
public enum TipoRegistro {
    /**
     * Registro automático mediante escaneo de código QR
     */
    QR_AUTO,
    
    /**
     * Registro manual realizado por el personal del gimnasio
     */
    MANUAL_STAFF,
    
    /**
     * Registro mediante molinete/torniquete electrónico
     */
    MOLINETE
}
