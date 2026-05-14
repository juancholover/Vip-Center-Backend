package com.gimnasio.fit.specification;

import com.gimnasio.fit.entity.Cliente;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * JPA Specification para filtrar clientes/suscripciones dinámicamente (HU-28).
 * Permite combinar filtros de estado, rango de fechas y días de anticipación.
 */
public class SuscripcionSpecification {

    private SuscripcionSpecification() {
        // Utility class
    }

    /**
     * Construye una Specification combinando todos los filtros proporcionados.
     *
     * @param estado           Estado de la suscripción: "activa", "vencida", "por_vencer"
     * @param fechaInicio      Fecha inicio del rango de vencimiento (opcional)
     * @param fechaFin         Fecha fin del rango de vencimiento (opcional)
     * @param diasAnticipacion Días de anticipación para "por_vencer" (default 15)
     * @return Specification<Cliente> combinada
     */
    public static Specification<Cliente> conFiltros(
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Integer diasAnticipacion
    ) {
        Specification<Cliente> spec = Specification.where(null);

        // Filtrar por estado
        if (estado != null && !estado.isBlank()) {
            spec = spec.and(porEstado(estado, diasAnticipacion));
        }

        // Filtrar por rango de fechas de vencimiento
        if (fechaInicio != null) {
            spec = spec.and(fechaVencimientoDespuesDe(fechaInicio));
        }
        if (fechaFin != null) {
            spec = spec.and(fechaVencimientoAntesDe(fechaFin));
        }

        return spec;
    }

    /**
     * Filtra por estado de suscripción calculado dinámicamente.
     */
    private static Specification<Cliente> porEstado(String estado, Integer diasAnticipacion) {
        return (root, query, cb) -> {
            LocalDate hoy = LocalDate.now();
            int dias = (diasAnticipacion != null && diasAnticipacion > 0) ? diasAnticipacion : 15;

            switch (estado.toLowerCase()) {
                case "activa":
                    // Membresía vigente (fecha_vencimiento >= hoy) y QR activo
                    return cb.and(
                        cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), hoy),
                        cb.equal(root.get("qrActivo"), true)
                    );

                case "vencida":
                    // Membresía vencida (fecha_vencimiento < hoy)
                    return cb.and(
                        cb.isNotNull(root.get("fechaVencimiento")),
                        cb.lessThan(root.get("fechaVencimiento"), hoy)
                    );

                case "por_vencer":
                    // Membresía próxima a vencer (entre hoy y hoy + diasAnticipacion)
                    LocalDate limite = hoy.plusDays(dias);
                    return cb.and(
                        cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), hoy),
                        cb.lessThanOrEqualTo(root.get("fechaVencimiento"), limite),
                        cb.equal(root.get("qrActivo"), true)
                    );

                case "sin_membresia":
                    return cb.isNull(root.get("fechaVencimiento"));

                default:
                    return cb.conjunction(); // Sin filtro
            }
        };
    }

    /**
     * Filtra clientes cuya fecha de vencimiento sea >= fechaInicio.
     */
    private static Specification<Cliente> fechaVencimientoDespuesDe(LocalDate fechaInicio) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), fechaInicio);
    }

    /**
     * Filtra clientes cuya fecha de vencimiento sea <= fechaFin.
     */
    private static Specification<Cliente> fechaVencimientoAntesDe(LocalDate fechaFin) {
        return (root, query, cb) ->
            cb.lessThanOrEqualTo(root.get("fechaVencimiento"), fechaFin);
    }
}
