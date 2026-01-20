package com.fiap.vaga_liberada_agenda.service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacaoService {

    private final SqsTemplate sqsTemplate;
    private static final String QUEUE_NAME = "notificacao-agenda-queue";

    public void enviar(String mensagem) {
        log.info("Enviando mensagem para fila SQS: {}", QUEUE_NAME);
        sqsTemplate.send(to -> to.queue(QUEUE_NAME).payload(mensagem));
        log.info("Mensagem enviada com sucesso");
    }
}
