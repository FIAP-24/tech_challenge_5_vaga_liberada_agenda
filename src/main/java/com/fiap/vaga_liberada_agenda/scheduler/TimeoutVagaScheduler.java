package com.fiap.vaga_liberada_agenda.scheduler;

import com.fiap.vaga_liberada_agenda.entity.Consulta;
import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
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
public class TimeoutVagaScheduler {

    private final ConsultaRepository consultaRepository;
    private final LiberacaoVagaService liberacaoVagaService;

    // Executa a cada 30 minutos (1800000 ms)
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void processarTimeoutsDeVagas() {
        log.info("Verificando timeouts de vagas oferecidas...");

        try {
            // Busca consultas liberadas que têm vaga oferecida
            List<Consulta> consultasLiberadas = consultaRepository.findByStatus(StatusConsulta.LIBERADA);

            if (consultasLiberadas.isEmpty()) {
                log.debug("Nenhuma consulta liberada encontrada.");
                return;
            }

            int processadas = 0;
            for (Consulta consulta : consultasLiberadas) {
                if (consulta.getVagaOferecidaParaListaEspera() != null) {
                    try {
                        liberacaoVagaService.processarTimeoutVaga(consulta);
                        processadas++;
                    } catch (Exception e) {
                        log.error("Erro ao processar timeout da consulta ID: {}", consulta.getId(), e);
                    }
                }
            }

            if (processadas > 0) {
                log.info("Processados {} timeouts de vagas oferecidas.", processadas);
            }

        } catch (Exception e) {
            log.error("Erro ao processar timeouts de vagas", e);
        }
    }
}
