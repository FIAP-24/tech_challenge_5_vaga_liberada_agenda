package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.PacienteRequest;
import com.fiap.vaga_liberada_agenda.dto.response.PacienteResponse;
import com.fiap.vaga_liberada_agenda.entity.Paciente;
import com.fiap.vaga_liberada_agenda.mapper.PacienteMapper;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.repository.ListaEsperaRepository;
import com.fiap.vaga_liberada_agenda.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;
    private final ConsultaRepository consultaRepository;
    private final ListaEsperaRepository listaEsperaRepository;

    @Transactional
    public PacienteResponse criar(PacienteRequest request) {
        log.info("Criando novo paciente: {}", request.getNome());

        if (pacienteRepository.existsByCpf(request.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado: " + request.getCpf());
        }

        if (request.getCartaoSus() != null && !request.getCartaoSus().isBlank()) {
            if (pacienteRepository.existsByCartaoSus(request.getCartaoSus())) {
                throw new IllegalArgumentException("Cartão SUS já cadastrado: " + request.getCartaoSus());
            }
        }

        Paciente paciente = pacienteMapper.toEntity(request);
        Paciente pacienteSalvo = pacienteRepository.save(paciente);
        log.info("Paciente criado com sucesso. ID: {}", pacienteSalvo.getId());

        return pacienteMapper.toResponse(pacienteSalvo);
    }

    public PacienteResponse buscarPorId(Integer id) {
        log.info("Buscando paciente por ID: {}", id);
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + id));
        return pacienteMapper.toResponse(paciente);
    }

    public PacienteResponse buscarPorCpf(String cpf) {
        log.info("Buscando paciente por CPF: {}", cpf);
        Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com CPF: " + cpf));
        return pacienteMapper.toResponse(paciente);
    }

    public Page<PacienteResponse> listar(Pageable pageable) {
        log.info("Listando pacientes - página: {}, tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        return pacienteRepository.findAll(pageable)
                .map(pacienteMapper::toResponse);
    }

    public List<PacienteResponse> listarTodos() {
        log.info("Listando todos os pacientes");
        return pacienteRepository.findAll().stream()
                .map(pacienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PacienteResponse atualizar(Integer id, PacienteRequest request) {
        log.info("Atualizando paciente ID: {}", id);

        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + id));

        if (!paciente.getCpf().equals(request.getCpf())) {
            if (pacienteRepository.existsByCpf(request.getCpf())) {
                throw new IllegalArgumentException("CPF já cadastrado: " + request.getCpf());
            }
        }

        if (request.getCartaoSus() != null && !request.getCartaoSus().isBlank()) {
            if (!request.getCartaoSus().equals(paciente.getCartaoSus()) &&
                    pacienteRepository.existsByCartaoSus(request.getCartaoSus())) {
                throw new IllegalArgumentException("Cartão SUS já cadastrado: " + request.getCartaoSus());
            }
        }

        pacienteMapper.updateEntityFromRequest(request, paciente);

        Paciente pacienteAtualizado = pacienteRepository.save(paciente);
        log.info("Paciente atualizado com sucesso. ID: {}", pacienteAtualizado.getId());

        return pacienteMapper.toResponse(pacienteAtualizado);
    }

    @Transactional
    public void deletar(Integer id) {
        log.info("Deletando paciente ID: {}", id);
        
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + id));
        
        // Verifica se há consultas associadas
        if (consultaRepository.existsByPacienteId(id)) {
            throw new IllegalArgumentException("Não é possível deletar paciente com consultas associadas. ID: " + id);
        }
        
        // Verifica se está na lista de espera ativa
        if (listaEsperaRepository.existsPacienteAtivoNaLista(id)) {
            throw new IllegalArgumentException("Não é possível deletar paciente que está na lista de espera ativa. ID: " + id);
        }
        
        pacienteRepository.deleteById(id);
        log.info("Paciente deletado com sucesso. ID: {}", id);
    }

}
