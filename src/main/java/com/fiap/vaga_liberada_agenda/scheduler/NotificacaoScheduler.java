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
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioJanela = agora.plusHours(24).minusMinutes(30); // 23h30min a partir de agora
        LocalDateTime fimJanela = agora.plusHours(24).plusMinutes(30);     // 24h30min a partir de agora

        List<Consulta> consultasProximas = consultaRepository.buscarConsultasParaNotificar(
                inicioJanela,
                fimJanela,
                StatusConsulta.AGENDADA
        );

        if (consultasProximas.isEmpty()) {
            return;
        }

        log.info("Encontradas {} consultas para confirmar.", consultasProximas.size());

        for (Consulta consulta : consultasProximas) {
            try {
                // 1. Monta a mensagem JSON (formato simples)
                String mensagemJson = String.format(
                        "{\"pacienteId\": %d, \"consultaId\": %d, \"mensagem\": \"Olá %s, confirme sua consulta para amanhã às %s na %s.\"}",
                        consulta.getPaciente().getId(),
                        consulta.getId(),
                        consulta.getPaciente().getNome(),
                        consulta.getDataHora().toLocalTime(),
                        consulta.getUnidade().getNome()
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
