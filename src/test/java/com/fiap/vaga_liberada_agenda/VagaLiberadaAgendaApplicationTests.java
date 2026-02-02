package com.fiap.vaga_liberada_agenda;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class VagaLiberadaAgendaApplicationTests {

	@MockBean
	private SqsTemplate sqsTemplate;

	@Test
	void contextLoads() {
	}

}
