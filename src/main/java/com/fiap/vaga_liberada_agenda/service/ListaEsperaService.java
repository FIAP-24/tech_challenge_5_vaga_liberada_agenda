package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.ListaEsperaRequest;
import com.fiap.vaga_liberada_agenda.dto.response.ListaEsperaResponse;
import com.fiap.vaga_liberada_agenda.entity.*;
import com.fiap.vaga_liberada_agenda.mapper.ListaEsperaMapper;
import com.fiap.vaga_liberada_agenda.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListaEsperaService {

    private final ListaEsperaRepository listaEsperaRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final MedicoRepository medicoRepository;
    private final UnidadeSaudeRepository unidadeSaudeRepository;
    private final ListaEsperaMapper listaEsperaMapper;

    @Transactional
    public ListaEsperaResponse adicionarNaLista(ListaEsperaRequest request) {
        log.info("Adicionando paciente {} na lista de espera", request.getPacienteId());

        // Validações
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + request.getPacienteId()));

        Especialidade especialidade = especialidadeRepository.findById(request.getEspecialidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com ID: " + request.getEspecialidadeId()));

        // Verificar se já existe na lista de espera ativa
        List<ListaEspera> existentes = listaEsperaRepository.findByPacienteIdAndStatus(
                request.getPacienteId(), StatusListaEspera.ATIVA);
        if (!existentes.isEmpty()) {
            throw new IllegalArgumentException("Paciente já está na lista de espera ativa");
        }

        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setPaciente(paciente);
        listaEspera.setEspecialidade(especialidade);
        listaEspera.setStatus(StatusListaEspera.ATIVA);
        listaEspera.setPrioridade(request.getPrioridade() != null ? request.getPrioridade() : 0);

        if (request.getMedicoId() != null) {
            Medico medico = medicoRepository.findById(request.getMedicoId())
                    .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com ID: " + request.getMedicoId()));
            listaEspera.setMedico(medico);
        }

        if (request.getUnidadeId() != null) {
            UnidadeSaude unidade = unidadeSaudeRepository.findById(request.getUnidadeId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidade de saúde não encontrada com ID: " + request.getUnidadeId()));
            listaEspera.setUnidade(unidade);
        }

        ListaEspera salva = listaEsperaRepository.save(listaEspera);
        log.info("Paciente adicionado na lista de espera. ID: {}", salva.getId());

        return listaEsperaMapper.toResponseWithDetails(salva);
    }

    public List<ListaEsperaResponse> listarPorFiltros(Integer especialidadeId, Integer medicoId, Integer unidadeId) {
        log.info("Listando lista de espera com filtros - especialidade: {}, médico: {}, unidade: {}",
                especialidadeId, medicoId, unidadeId);

        List<ListaEspera> lista = listaEsperaRepository.findByFiltros(
                StatusListaEspera.ATIVA, especialidadeId, medicoId, unidadeId);

        return lista.stream()
                .map(listaEsperaMapper::toResponseWithDetails)
                .collect(Collectors.toList());
    }

    public Optional<ListaEsperaResponse> obterProximoDaFila(Integer especialidadeId, Integer medicoId, Integer unidadeId) {
        log.info("Obtendo próximo da fila - especialidade: {}, médico: {}, unidade: {}",
                especialidadeId, medicoId, unidadeId);

        Optional<ListaEspera> proximo = listaEsperaRepository.findFirstByFiltros(
                StatusListaEspera.ATIVA, especialidadeId, medicoId, unidadeId);

        return proximo.map(listaEsperaMapper::toResponseWithDetails);
    }

    @Transactional
    public void removerDaLista(Integer id) {
        log.info("Removendo da lista de espera. ID: {}", id);

        ListaEspera listaEspera = listaEsperaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro não encontrado na lista de espera com ID: " + id));

        if (listaEspera.getStatus() == StatusListaEspera.AGUARDANDO_RESPOSTA) {
            throw new IllegalArgumentException("Não é possível remover registro que está aguardando resposta de vaga oferecida");
        }

        listaEspera.setStatus(StatusListaEspera.CANCELADA);
        listaEsperaRepository.save(listaEspera);
        log.info("Registro removido da lista de espera. ID: {}", id);
    }

    public ListaEsperaResponse buscarPorId(Integer id) {
        log.info("Buscando lista de espera por ID: {}", id);
        ListaEspera listaEspera = listaEsperaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro não encontrado na lista de espera com ID: " + id));
        return listaEsperaMapper.toResponseWithDetails(listaEspera);
    }

    public List<ListaEsperaResponse> listarTodos() {
        log.info("Listando todos os registros da lista de espera");
        return listaEsperaRepository.findAll().stream()
                .map(listaEsperaMapper::toResponseWithDetails)
                .collect(Collectors.toList());
    }
}
