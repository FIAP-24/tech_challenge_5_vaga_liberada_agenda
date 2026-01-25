package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.MedicoRequest;
import com.fiap.vaga_liberada_agenda.dto.response.MedicoResponse;
import com.fiap.vaga_liberada_agenda.entity.Especialidade;
import com.fiap.vaga_liberada_agenda.entity.Medico;
import com.fiap.vaga_liberada_agenda.entity.UnidadeSaude;
import com.fiap.vaga_liberada_agenda.mapper.MedicoMapper;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.repository.EspecialidadeRepository;
import com.fiap.vaga_liberada_agenda.repository.MedicoRepository;
import com.fiap.vaga_liberada_agenda.repository.UnidadeSaudeRepository;
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
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final UnidadeSaudeRepository unidadeSaudeRepository;
    private final MedicoMapper medicoMapper;
    private final ConsultaRepository consultaRepository;

    @Transactional
    public MedicoResponse criar(MedicoRequest request) {
        log.info("Criando novo médico: {}", request.getNome());

        if (medicoRepository.existsByCrm(request.getCrm())) {
            throw new IllegalArgumentException("CRM já cadastrado: " + request.getCrm());
        }

        Especialidade especialidade = especialidadeRepository.findById(request.getEspecialidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com ID: " + request.getEspecialidadeId()));

        UnidadeSaude unidade = unidadeSaudeRepository.findById(request.getUnidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Unidade de Saúde não encontrada com ID: " + request.getUnidadeId()));

        Medico medico = medicoMapper.toEntity(request);
        medico.setEspecialidade(especialidade);
        medico.setUnidade(unidade);
        medico.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);

        Medico medicoSalvo = medicoRepository.save(medico);
        log.info("Médico criado com sucesso. ID: {}", medicoSalvo.getId());

        return medicoMapper.toResponse(medicoSalvo);
    }

    public MedicoResponse buscarPorId(Integer id) {
        log.info("Buscando médico por ID: {}", id);
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com ID: " + id));
        return medicoMapper.toResponse(medico);
    }

    public MedicoResponse buscarPorCrm(String crm) {
        log.info("Buscando médico por CRM: {}", crm);
        Medico medico = medicoRepository.findByCrm(crm)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com CRM: " + crm));
        return medicoMapper.toResponse(medico);
    }

    public Page<MedicoResponse> listarComFiltros(Pageable pageable, Integer especialidadeId, Integer unidadeId, Boolean ativo) {
        log.info("Listando médicos com filtros - especialidade: {}, unidade: {}, ativo: {}, página: {}, tamanho: {}", 
                especialidadeId, unidadeId, ativo, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Medico> medicos;
        
        if (especialidadeId != null && unidadeId != null && ativo != null) {
            medicos = medicoRepository.findByEspecialidadeIdAndUnidadeIdAndAtivo(especialidadeId, unidadeId, ativo, pageable);
        } else if (especialidadeId != null && unidadeId != null) {
            medicos = medicoRepository.findByEspecialidadeIdAndUnidadeId(especialidadeId, unidadeId, pageable);
        } else if (especialidadeId != null && ativo != null) {
            medicos = medicoRepository.findByEspecialidadeIdAndAtivo(especialidadeId, ativo, pageable);
        } else if (unidadeId != null && ativo != null) {
            medicos = medicoRepository.findByUnidadeIdAndAtivo(unidadeId, ativo, pageable);
        } else if (especialidadeId != null) {
            medicos = medicoRepository.findByEspecialidadeId(especialidadeId, pageable);
        } else if (unidadeId != null) {
            medicos = medicoRepository.findByUnidadeId(unidadeId, pageable);
        } else if (ativo != null) {
            medicos = medicoRepository.findByAtivo(ativo, pageable);
        } else {
            medicos = medicoRepository.findAll(pageable);
        }
        
        return medicos.map(medicoMapper::toResponse);
    }

    @Transactional
    public MedicoResponse atualizar(Integer id, MedicoRequest request) {
        log.info("Atualizando médico ID: {}", id);

        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com ID: " + id));

        if (!medico.getCrm().equals(request.getCrm())) {
            if (medicoRepository.existsByCrm(request.getCrm())) {
                throw new IllegalArgumentException("CRM já cadastrado: " + request.getCrm());
            }
        }

        Especialidade especialidade = especialidadeRepository.findById(request.getEspecialidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com ID: " + request.getEspecialidadeId()));

        UnidadeSaude unidade = unidadeSaudeRepository.findById(request.getUnidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Unidade de Saúde não encontrada com ID: " + request.getUnidadeId()));

        medicoMapper.updateEntityFromRequest(request, medico);
        medico.setEspecialidade(especialidade);
        medico.setUnidade(unidade);
        if (request.getAtivo() != null) {
            medico.setAtivo(request.getAtivo());
        }

        Medico medicoAtualizado = medicoRepository.save(medico);
        log.info("Médico atualizado com sucesso. ID: {}", medicoAtualizado.getId());

        return medicoMapper.toResponse(medicoAtualizado);
    }

    @Transactional
    public void desativar(Integer id) {
        log.info("Desativando médico ID: {}", id);
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com ID: " + id));
        medico.setAtivo(false);
        medicoRepository.save(medico);
        log.info("Médico desativado com sucesso. ID: {}", id);
    }

    @Transactional
    public void ativar(Integer id) {
        log.info("Ativando médico ID: {}", id);
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com ID: " + id));
        medico.setAtivo(true);
        medicoRepository.save(medico);
        log.info("Médico ativado com sucesso. ID: {}", id);
    }

    @Transactional
    public void deletar(Integer id) {
        log.info("Deletando médico ID: {}", id);
        
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com ID: " + id));
        
        // Verifica se há consultas associadas
        if (consultaRepository.existsByMedicoId(id)) {
            throw new IllegalArgumentException("Não é possível deletar médico com consultas associadas. ID: " + id);
        }
        
        medicoRepository.deleteById(id);
        log.info("Médico deletado com sucesso. ID: {}", id);
    }
}
