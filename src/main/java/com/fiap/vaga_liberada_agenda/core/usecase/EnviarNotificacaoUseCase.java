package com.fiap.vaga_liberada_agenda.core.usecase;

import com.fiap.vaga_liberada_agenda.core.gateway.NotificacaoGateway;
import org.springframework.stereotype.Service;

@Service
public class EnviarNotificacaoUseCase {

    private final NotificacaoGateway notificacaoGateway;

    public EnviarNotificacaoUseCase(NotificacaoGateway notificacaoGateway) {
        this.notificacaoGateway = notificacaoGateway;
    }

    public void executar(String mensagem) {

        System.out.println("UseCase: Iniciando envio de notificação...");
        notificacaoGateway.enviar(mensagem);
    }
}