package com.gimnasio.fit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Habilita tareas programadas (@Scheduled)
@EnableAsync // Habilita métodos asíncronos (@Async)
@EnableJpaRepositories(basePackages = "com.gimnasio.fit.repository")
public class VipCenterFitApplication {

	public static void main(String[] args) {
		SpringApplication.run(VipCenterFitApplication.class, args);
	}

}
