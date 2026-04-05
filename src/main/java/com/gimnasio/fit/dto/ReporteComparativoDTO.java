package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteComparativoDTO {
    private String metrica;           // "Ingresos", "Asistencias", "Clientes Nuevos"
    private Double valorPeriodoActual;
    private Double valorPeriodoAnterior;
    private Double diferencia;
    private Double porcentajeCambio;
    private String tendencia;         // "subida", "bajada", "estable"
    private String periodo;           // "Octubre 2025"
}
