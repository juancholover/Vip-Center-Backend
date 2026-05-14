package com.gimnasio.fit;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {
    @Test
    public void genHash() {
        System.out.println("HASH_GENERADO=" + new BCryptPasswordEncoder().encode("Admin123!"));
    }
}
