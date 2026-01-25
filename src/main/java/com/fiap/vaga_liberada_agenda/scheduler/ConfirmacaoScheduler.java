package com.fiap.vaga_liberada_agenda.scheduler;

import com.fiap.vaga_liberada_agenda.entity.Consulta;
import com.fiap.vaga_liberada_agenda.service.ConsultaService;
import com.fiap.vaga_liberada_agenda.service.LiberacaoVagaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmacaoScheduler {

    private final ConsultaService consultaService;
    private final LiberacaoVagaService liberacaoVagaService;

    // Executa a cada hora (3600000 ms)
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void verificarConsultasNaoConfirmadas() {
        log.info("Verificando consultas não confirmadas para liberação de vagas...");

        try {
            List<Consulta> consultasNaoConfirmadas = consultaService.verificarConsultasNaoConfirmadas();

            if (consultasNaoConfirmadas.isEmpty()) {
                log.debug("Nenhuma consulta não confirmada encontrada.");
                return;
            }

            log.info("Encontradas {} consultas não confirmadas. Liberando vagas...", consultasNaoConfirmadas.size());

            for (Consulta consulta : consultasNaoConfirmadas) {
                try {
                    liberacaoVagaService.liberarVaga(consulta);
                    log.info("Vaga liberada para consulta ID: {}", consulta.getId());
                } catch (Exception e) {
                    log.error("Erro ao liberar vaga da consulta ID: {}", consulta.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("Erro ao verificar consultas não confirmadas", e);
        }
    }
}
