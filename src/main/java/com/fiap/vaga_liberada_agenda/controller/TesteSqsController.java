package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste/sqs")
@RequiredArgsConstructor
public class TesteSqsController {

    private final NotificacaoService notificacaoService;

    @PostMapping
    public String enviarMensagemDeTeste(@RequestBody String mensagem) {
        notificacaoService.enviar(mensagem);
        return "Mensagem enviada para processamento: " + mensagem;
    }
}
