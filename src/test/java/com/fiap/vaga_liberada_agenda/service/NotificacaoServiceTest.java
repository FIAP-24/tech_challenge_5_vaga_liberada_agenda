package com.fiap.vaga_liberada_agenda.service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @InjectMocks
    private NotificacaoService notificacaoService;

    @Test
    void enviar_deveChamarSqsTemplate() {
        String mensagem = "{\"pacienteId\": 1, \"mensagem\": \"Teste\"}";

        notificacaoService.enviar(mensagem);

        verify(sqsTemplate).send(any());
    }
}
