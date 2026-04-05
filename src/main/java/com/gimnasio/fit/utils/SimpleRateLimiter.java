package com.gimnasio.fit.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador muy simple en memoria por clave (e.g., token+IP).
 * Permite 1 acción por ventana (default 2s) por clave.
 */
@Component
public class SimpleRateLimiter {

    private final long windowMillis;
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();

    public SimpleRateLimiter(@Value("${app.rateLimit.windowMillis:2000}") long windowMillis) {
        this.windowMillis = windowMillis;
    }

    /**
     * Retorna true si se permite ejecutar para la clave; false si debe esperar
     * hasta que la ventana expire.
     */
    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        Long prev = lastSeen.get(key);
        if (prev == null || (now - prev) >= windowMillis) {
            lastSeen.put(key, now);
            return true;
        }
        return false;
    }
}
