package com.gimnasio.fit.service;

import com.gimnasio.fit.dto.ActualizarClienteRequest;
import com.gimnasio.fit.dto.ClienteResponse;
import com.gimnasio.fit.dto.CrearClienteRequest;

import java.util.List;

public interface ClienteService {

    ClienteResponse crear(CrearClienteRequest request, Long registradoPorId);

    ClienteResponse actualizar(Long id, ActualizarClienteRequest request);

    ClienteResponse buscarPorId(Long id);

    ClienteResponse buscarPorTelefono(String telefono);

    ClienteResponse buscarPorQr(String qrAcceso);

    List<ClienteResponse> buscarPorNombreOApellido(String termino);

    List<ClienteResponse> listarTodos();

    List<ClienteResponse> listarActivos();

    List<ClienteResponse> listarVencidos();

    List<ClienteResponse> listarSinMembresia();

    List<ClienteResponse> listarProximosAVencer(int dias);

    void eliminar(Long id);
    
    ClienteResponse regenerarQr(Long id);
    
    /**
     * Corrige todos los clientes que no tienen QR generándoles uno automáticamente.
     * @return Número de clientes corregidos
     */
    int corregirClientesSinQr();
    
    /**
     * Obtiene el ID de un usuario por su email.
     * @param email Email del usuario
     * @return ID del usuario o null si no existe
     */
    Long obtenerIdUsuarioPorEmail(String email);
}
