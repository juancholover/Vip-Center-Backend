package com.gimnasio.fit.service;

import com.gimnasio.fit.controller.AccesoController.*;

import java.util.List;

public interface AccesoService {
    
    /**
     * Verifica si un cliente tiene acceso permitido al gimnasio usando token QR
     * @param qrToken Token UUID del QR del cliente
     * @return Respuesta con información de acceso y detalles del cliente
     */
    VerificarAccesoResponse verificarAccesoConQR(String qrToken);
    
    /**
     * Verifica si un cliente tiene acceso permitido al gimnasio (legacy - por ID)
     * @param clienteId ID del cliente
     * @return Respuesta con información de acceso y detalles del cliente
     */
    VerificarAccesoResponse verificarAcceso(Long clienteId);
    
    /**
     * Registra la asistencia de un cliente usando token QR
     * @param qrToken Token UUID del QR del cliente
     * @param tipoRegistro "QR_AUTO" o "MANUAL_STAFF"
     * @param empleadoId ID del empleado que registra (opcional)
     * @return Respuesta con confirmación de registro
     */
    RegistrarAsistenciaResponse registrarAsistenciaConQR(String qrToken, String tipoRegistro, Long empleadoId);
    
    /**
     * Registra la asistencia de un cliente (legacy - por ID)
     * @param clienteId ID del cliente
     * @param tipoRegistro "QR_AUTO" o "MANUAL_STAFF"
     * @param empleadoId ID del empleado que registra (opcional)
     * @return Respuesta con confirmación de registro
     */
    RegistrarAsistenciaResponse registrarAsistencia(Long clienteId, String tipoRegistro, Long empleadoId);
    
    /**
     * Obtiene las últimas asistencias registradas
     * @param limite Número máximo de resultados
     * @return Lista de asistencias recientes
     */
    List<AsistenciaRecienteDTO> obtenerAsistenciasRecientes(int limite);
    
    /**
     * Busca clientes por nombre o teléfono
     * @param query Término de búsqueda
     * @return Lista de clientes que coinciden
     */
    List<ClienteBusquedaDTO> buscarClientes(String query);
    
    /**
     * Cuenta el número de ingresos del día actual
     * @return Número total de ingresos hoy
     */
    int contarIngresosDia();
}
