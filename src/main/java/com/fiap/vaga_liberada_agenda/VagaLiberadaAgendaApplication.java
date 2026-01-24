package com.fiap.vaga_liberada_agenda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VagaLiberadaAgendaApplication {

	public static void main(String[] args) {
		SpringApplication.run(VagaLiberadaAgendaApplication.class, args);
	}

}
