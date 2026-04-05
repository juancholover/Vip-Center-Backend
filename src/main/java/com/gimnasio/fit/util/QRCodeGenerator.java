package com.gimnasio.fit.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utilidad para generar códigos QR optimizados para lectura rápida
 */
public class QRCodeGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String NUMERIC = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Genera un UUID estándar (formato: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)
     * - Longitud: 36 caracteres
     * - Complejidad: Media
     * - Legibilidad: Buena
     * @return UUID en formato estándar
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Genera un UUID sin guiones (formato: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx)
     * - Longitud: 32 caracteres
     * - Complejidad: Media
     * - Legibilidad: Muy buena (QR más simple)
     * @return UUID sin guiones
     */
    public static String generateSimpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Genera un código alfanumérico corto
     * - Longitud: 12 caracteres por defecto
     * - Complejidad: Baja
     * - Legibilidad: Excelente (QR muy simple)
     * @param length Longitud del código (recomendado: 12-16)
     * @return Código alfanumérico en mayúsculas
     */
    public static String generateAlphanumeric(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return code.toString();
    }

    /**
     * Genera un código alfanumérico de 12 caracteres
     * Ejemplo: A1B2C3D4E5F6
     * - Longitud: 12 caracteres
     * - QR muy simple y fácil de leer
     * @return Código alfanumérico de 12 caracteres
     */
    public static String generateShortCode() {
        return generateAlphanumeric(12);
    }

    /**
     * Genera un código numérico puro
     * - Longitud: configurable
     * - Complejidad: Muy baja
     * - Legibilidad: Máxima (QR más simple posible)
     * @param length Longitud del código (recomendado: 8-12)
     * @return Código numérico puro
     */
    public static String generateNumeric(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(NUMERIC.charAt(random.nextInt(NUMERIC.length())));
        }
        return code.toString();
    }

    /**
     * Genera un código híbrido: prefijo + timestamp + aleatorio
     * Ejemplo: GYM20251103A1B2
     * - Longitud: ~18 caracteres
     * - Incluye fecha para ordenamiento
     * - Legibilidad: Muy buena
     * @param prefix Prefijo identificador (ej: "GYM")
     * @return Código híbrido
     */
    public static String generateHybridCode(String prefix) {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5, 13); // 8 dígitos
        String random = generateAlphanumeric(6);
        return prefix + timestamp + random;
    }

    /**
     * Genera un código optimizado para QR de gimnasio
     * Usa UUID sin guiones para máxima simplicidad y legibilidad
     * @return Código QR optimizado
     */
    public static String generateOptimizedQR() {
        return generateSimpleUUID();
    }
}
