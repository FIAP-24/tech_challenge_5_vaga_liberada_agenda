package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.entity.*;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.repository.ListaEsperaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiberacaoVagaService {

    private final ConsultaRepository consultaRepository;
    private final ListaEsperaRepository listaEsperaRepository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public void liberarVaga(Consulta consulta) {
        log.info("Liberando vaga para consulta ID: {}", consulta.getId());

        // Marca consulta como LIBERADA
        consulta.setStatus(StatusConsulta.LIBERADA);
        consultaRepository.save(consulta);

        // Busca próximo da lista de espera
        Integer especialidadeId = consulta.getMedico().getEspecialidade().getId();
        Integer medicoId = consulta.getMedico().getId();
        Integer unidadeId = consulta.getUnidade().getId();

        Optional<ListaEspera> proximo = listaEsperaRepository.findFirstByFiltros(
                StatusListaEspera.ATIVA, especialidadeId, medicoId, unidadeId);

        if (proximo.isEmpty()) {
            // Tenta buscar apenas por especialidade
            proximo = listaEsperaRepository.findFirstByFiltros(
                    StatusListaEspera.ATIVA, especialidadeId, null, null);
        }

        if (proximo.isPresent()) {
            oferecerVagaParaListaEspera(consulta, proximo.get());
        } else {
            log.info("Nenhum paciente encontrado na lista de espera para a vaga liberada. Consulta ID: {}", consulta.getId());
        }
    }

    @Transactional
    public void oferecerVagaParaListaEspera(Consulta consulta, ListaEspera listaEspera) {
        log.info("Oferecendo vaga da consulta ID: {} para paciente da lista de espera ID: {}",
                consulta.getId(), listaEspera.getId());

        // Atualiza lista de espera
        listaEspera.setStatus(StatusListaEspera.AGUARDANDO_RESPOSTA);
        listaEspera.setConsultaOferecida(consulta);
        listaEspera.setDataOferta(LocalDateTime.now());
        listaEsperaRepository.save(listaEspera);

        // Atualiza consulta
        consulta.setVagaOferecidaParaListaEspera(listaEspera);
        consulta.setVagaOferecidaEm(LocalDateTime.now());
        consultaRepository.save(consulta);

        // Envia notificação
        String mensagemJson = String.format(
                "{\"pacienteId\": %d, \"consultaId\": %d, \"listaEsperaId\": %d, " +
                "\"mensagem\": \"Olá %s, uma vaga foi liberada! Você tem uma consulta disponível em %s às %s com Dr(a). %s na %s. " +
                "Por favor, confirme sua presença através do sistema.\"}",
                listaEspera.getPaciente().getId(),
                consulta.getId(),
                listaEspera.getId(),
                listaEspera.getPaciente().getNome(),
                consulta.getDataHora().toLocalDate(),
                consulta.getDataHora().toLocalTime(),
                consulta.getMedico().getNome(),
                consulta.getUnidade().getNome()
        );

        notificacaoService.enviar(mensagemJson);
        log.info("Notificação de vaga liberada enviada para paciente ID: {}", listaEspera.getPaciente().getId());
    }

    @Transactional
    public void aceitarVaga(Integer consultaId, Integer listaEsperaId) {
        log.info("Aceitando vaga - Consulta ID: {}, Lista Espera ID: {}", consultaId, listaEsperaId);

        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada com ID: " + consultaId));

        ListaEspera listaEspera = listaEsperaRepository.findById(listaEsperaId)
                .orElseThrow(() -> new IllegalArgumentException("Registro não encontrado na lista de espera com ID: " + listaEsperaId));

        if (consulta.getStatus() != StatusConsulta.LIBERADA) {
            throw new IllegalArgumentException("Esta vaga não está mais disponível");
        }

        if (listaEspera.getStatus() != StatusListaEspera.AGUARDANDO_RESPOSTA) {
            throw new IllegalArgumentException("Esta vaga não está mais disponível para este paciente");
        }

        // Atualiza consulta com o novo paciente
        consulta.setPaciente(listaEspera.getPaciente());
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setConfirmadaEm(LocalDateTime.now());
        consultaRepository.save(consulta);

        // Atualiza lista de espera
        listaEspera.setStatus(StatusListaEspera.ATENDIDA);
        listaEsperaRepository.save(listaEspera);

        log.info("Vaga aceita com sucesso. Consulta ID: {} agora pertence ao paciente ID: {}",
                consultaId, listaEspera.getPaciente().getId());
    }

    @Transactional
    public void processarTimeoutVaga(Consulta consulta) {
        log.info("Processando timeout de vaga oferecida. Consulta ID: {}", consulta.getId());

        if (consulta.getVagaOferecidaParaListaEspera() == null) {
            return;
        }

        ListaEspera listaEspera = consulta.getVagaOferecidaParaListaEspera();

        // Verifica se passou mais de 2 horas desde a oferta (configurável)
        if (listaEspera.getDataOferta() != null &&
            LocalDateTime.now().isAfter(listaEspera.getDataOferta().plusHours(2))) {

            log.info("Timeout da vaga. Oferecendo para próximo da fila. Consulta ID: {}", consulta.getId());

            // Volta a lista de espera para ATIVA
            listaEspera.setStatus(StatusListaEspera.ATIVA);
            listaEspera.setConsultaOferecida(null);
            listaEspera.setDataOferta(null);
            listaEsperaRepository.save(listaEspera);

            // Limpa a oferta da consulta
            consulta.setVagaOferecidaParaListaEspera(null);
            consulta.setVagaOferecidaEm(null);
            consultaRepository.save(consulta);

            // Oferece para próximo da fila
            Integer especialidadeId = consulta.getMedico().getEspecialidade().getId();
            Integer medicoId = consulta.getMedico().getId();
            Integer unidadeId = consulta.getUnidade().getId();

            Optional<ListaEspera> proximo = listaEsperaRepository.findFirstByFiltros(
                    StatusListaEspera.ATIVA, especialidadeId, medicoId, unidadeId);

            if (proximo.isEmpty()) {
                proximo = listaEsperaRepository.findFirstByFiltros(
                        StatusListaEspera.ATIVA, especialidadeId, null, null);
            }

            if (proximo.isPresent() && !proximo.get().getId().equals(listaEspera.getId())) {
                oferecerVagaParaListaEspera(consulta, proximo.get());
            }
        }
    }
}
