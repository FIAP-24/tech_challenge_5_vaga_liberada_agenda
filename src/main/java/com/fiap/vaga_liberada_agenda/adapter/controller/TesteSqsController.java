package com.fiap.vaga_liberada_agenda.adapter.controller;

import com.fiap.vaga_liberada_agenda.core.usecase.EnviarNotificacaoUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste/sqs")
public class TesteSqsController {

    private final EnviarNotificacaoUseCase enviarNotificacaoUseCase;

    public TesteSqsController(EnviarNotificacaoUseCase enviarNotificacaoUseCase) {
        this.enviarNotificacaoUseCase = enviarNotificacaoUseCase;
    }

    @PostMapping
    public String enviarMensagemDeTeste(@RequestBody String mensagem) {
        enviarNotificacaoUseCase.executar(mensagem);
        return "Mensagem enviada para processamento: " + mensagem;
    }
}