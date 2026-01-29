package com.fiap.vaga_liberada_agenda.scheduler;

import com.fiap.vaga_liberada_agenda.entity.Consulta;
import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.service.NotificacaoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoScheduler {
    private final ConsultaRepository consultaRepository;
    private final NotificacaoService notificacaoService;

    // Executa a cada 60 segundos (60000 ms)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void verificarConsultasProximas() {
        log.info("Verificando consultas próximas para notificação...");

        // Define a janela de tempo: Consultas que acontecem daqui a 24 horas (com margem de 1 hora)

        LocalDateTime inicioJanela = LocalDateTime.now();
        LocalDateTime fimJanela = inicioJanela.plusHours(24).plusMinutes(30);     // 24h30min a partir de agora

        // Busca consultas AGENDADAS e PENDENTE_CONFIRMACAO
        List<Consulta> consultasAgendadas = consultaRepository.buscarConsultasParaNotificar(
                inicioJanela,
                fimJanela,
                StatusConsulta.AGENDADA
        );
        
        List<Consulta> consultasPendentes = consultaRepository.buscarConsultasParaNotificar(
                inicioJanela,
                fimJanela,
                StatusConsulta.PENDENTE_CONFIRMACAO
        );
        
        List<Consulta> consultasProximas = new ArrayList<>();
        consultasProximas.addAll(consultasAgendadas);
        consultasProximas.addAll(consultasPendentes);

        if (consultasProximas.isEmpty()) {
            return;
        }

        log.info("Encontradas {} consultas para confirmar.", consultasProximas.size());


        // Define o formato desejado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Consulta consulta : consultasProximas) {
            try {
                // URL Base (Em produção seria o domínio real, aqui é localhost)
                String linkConfirmacao = "http://localhost:8080/confirmacao.html?id=" + consulta.getId();
                // 1. Monta a mensagem JSON (formato simples)
                String tipoMensagem = consulta.getStatus() == StatusConsulta.PENDENTE_CONFIRMACAO 
                        ? "confirme sua consulta" 
                        : "lembrete: sua consulta";

                String mensagemJson = String.format(
                        "{\"pacienteId\": %d, \"consultaId\": %d, \"mensagem\": \"Olá %s, %s para %s na %s. Por favor, confirme sua presença clicando no link: %s\" }",
                        consulta.getPaciente().getId(),
                        consulta.getId(),
                        consulta.getPaciente().getNome(),
                        tipoMensagem,
                        consulta.getDataHora().format(formatter),
                        consulta.getUnidade().getNome(),
                        linkConfirmacao
                );


                notificacaoService.enviar(mensagemJson);

                // Marca como notificada para não enviar novamente
                consulta.setLembreteEnviado(true);
                consultaRepository.save(consulta);

                log.info("Notificação enviada para consulta ID: {}", consulta.getId());

            } catch (Exception e) {
                log.error("Erro ao processar notificação da consulta ID: {}", consulta.getId(), e);
            }
        }
    }
}
