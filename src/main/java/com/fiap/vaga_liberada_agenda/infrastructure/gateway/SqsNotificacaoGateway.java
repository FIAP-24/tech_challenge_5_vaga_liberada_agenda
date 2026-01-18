package com.fiap.vaga_liberada_agenda.infrastructure.gateway;

import com.fiap.vaga_liberada_agenda.core.gateway.NotificacaoGateway;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Component;

@Component
public class SqsNotificacaoGateway implements NotificacaoGateway {

    private final SqsTemplate sqsTemplate;

    private static final String QUEUE_NAME = "notificacao-agenda-queue";

    public SqsNotificacaoGateway(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    @Override
    public void enviar(String mensagem) {
        System.out.println("Infra: Enviando mensagem para fila SQS: " + QUEUE_NAME);

        sqsTemplate.send(to -> to.queue(QUEUE_NAME).payload(mensagem));
    }
}