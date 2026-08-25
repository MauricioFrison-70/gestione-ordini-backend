package com.gestioneOrdini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GestioneOrdiniApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestioneOrdiniApplication.class, args);
	}

}
