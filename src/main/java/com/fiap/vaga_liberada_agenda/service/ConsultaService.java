package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.ConsultaRequest;
import com.fiap.vaga_liberada_agenda.dto.response.ConsultaResponse;
import com.fiap.vaga_liberada_agenda.entity.*;
import com.fiap.vaga_liberada_agenda.mapper.ConsultaMapper;
import com.fiap.vaga_liberada_agenda.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final UnidadeSaudeRepository unidadeSaudeRepository;
    private final ConsultaMapper consultaMapper;
    private final LiberacaoVagaService liberacaoVagaService;

    @Transactional
    public ConsultaResponse agendar(ConsultaRequest request) {
        log.info("Agendando consulta para paciente {} com médico {}", request.getPacienteId(), request.getMedicoId());

        // Validações
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + request.getPacienteId()));

        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com ID: " + request.getMedicoId()));

        if (!medico.getAtivo()) {
            throw new IllegalArgumentException("Médico não está ativo");
        }

        UnidadeSaude unidade = unidadeSaudeRepository.findById(request.getUnidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Unidade de saúde não encontrada com ID: " + request.getUnidadeId()));

        // Verificar se já existe consulta no mesmo horário
        if (consultaRepository.existsConsultaNoHorario(request.getMedicoId(), request.getDataHora())) {
            throw new IllegalArgumentException("Já existe uma consulta agendada para este médico no horário informado");
        }

        // Verificar se a data é futura
        if (request.getDataHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data da consulta deve ser futura");
        }

        Consulta consulta = new Consulta();
        consulta.setPaciente(paciente);
        consulta.setMedico(medico);
        consulta.setUnidade(unidade);
        consulta.setDataHora(request.getDataHora());
        consulta.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);
        consulta.setObservacoes(request.getObservacoes());
        consulta.setLembreteEnviado(false);

        consulta.setDataLimiteConfirmacao(request.getDataHora().minusMinutes(30)); // Ajustado para 30 minutos

        Consulta salva = consultaRepository.save(consulta);
        log.info("Consulta agendada com sucesso. ID: {}", salva.getId());

        return consultaMapper.toResponse(salva);
    }

    @Transactional
    public ConsultaResponse confirmarConsulta(Integer id) {
        log.info("Confirmando consulta ID: {}", id);

        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada com ID: " + id));

        if (consulta.getStatus() != StatusConsulta.PENDENTE_CONFIRMACAO) {
            throw new IllegalArgumentException("Apenas consultas pendentes de confirmação podem ser confirmadas");
        }

        if (consulta.getDataLimiteConfirmacao() != null && 
            LocalDateTime.now().isAfter(consulta.getDataLimiteConfirmacao())) {
            throw new IllegalArgumentException("Prazo para confirmação expirado. A consulta deve ser confirmada até 24 horas antes");
        }

        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setConfirmadaEm(LocalDateTime.now());

        Consulta atualizada = consultaRepository.save(consulta);
        log.info("Consulta confirmada com sucesso. ID: {}", atualizada.getId());

        return consultaMapper.toResponse(atualizada);
    }

    @Transactional
    public ConsultaResponse cancelarConsulta(Integer id) {
        log.info("Cancelando consulta ID: {}", id);

        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada com ID: " + id));

        if (consulta.getStatus() == StatusConsulta.REALIZADA) {
            throw new IllegalArgumentException("Não é possível cancelar uma consulta já realizada");
        }

        consulta.setStatus(StatusConsulta.CANCELADA);

        Consulta atualizada = consultaRepository.save(consulta);
        log.info("Consulta cancelada com sucesso. ID: {}", atualizada.getId());

        return consultaMapper.toResponse(atualizada);
    }

    @Transactional
    public ConsultaResponse desistirConsulta(Integer id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada com ID: " + id));

        if (consulta.getStatus() == StatusConsulta.REALIZADA) {
            throw new IllegalArgumentException("Não é possível cancelar uma consulta já realizada");
        }

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new IllegalArgumentException("Não é possível cancelar uma consulta cancelada");
        }

        consulta.setStatus(StatusConsulta.DESISTENCIA);

        Consulta atualizada = consultaRepository.save(consulta);
        log.info("Consulta cancelada com sucesso. ID: {}", atualizada.getId());

        liberacaoVagaService.liberarVaga(consulta);

        return consultaMapper.toResponse(atualizada);
    }

    public List<Consulta> verificarConsultasNaoConfirmadas() {
        log.info("Verificando consultas não confirmadas");
        LocalDateTime agora = LocalDateTime.now().plusHours(2);
        return consultaRepository.buscarConsultasNaoConfirmadas(StatusConsulta.PENDENTE_CONFIRMACAO, agora);
    }

    public ConsultaResponse buscarPorId(Integer id) {
        log.info("Buscando consulta por ID: {}", id);
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada com ID: " + id));
        return consultaMapper.toResponse(consulta);
    }

    public List<ConsultaResponse> listarPorPaciente(Integer pacienteId) {
        log.info("Listando consultas do paciente ID: {}", pacienteId);
        return consultaRepository.findByPacienteId(pacienteId).stream()
                .map(consultaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ConsultaResponse> listarPorStatus(StatusConsulta status) {
        log.info("Listando consultas com status: {}", status);
        return consultaRepository.findByStatus(status).stream()
                .map(consultaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ConsultaResponse> listarTodas() {
        log.info("Listando todas as consultas");
        return consultaRepository.findAll().stream()
                .map(consultaMapper::toResponse)
                .collect(Collectors.toList());
    }
}
