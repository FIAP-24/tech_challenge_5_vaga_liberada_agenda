package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.UnidadeSaudeRequest;
import com.fiap.vaga_liberada_agenda.dto.response.UnidadeSaudeResponse;
import com.fiap.vaga_liberada_agenda.entity.UnidadeSaude;
import com.fiap.vaga_liberada_agenda.mapper.UnidadeSaudeMapper;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
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
public class UnidadeSaudeService {

    private final UnidadeSaudeRepository unidadeSaudeRepository;
    private final UnidadeSaudeMapper unidadeSaudeMapper;
    private final MedicoRepository medicoRepository;
    private final ConsultaRepository consultaRepository;

    @Transactional
    public UnidadeSaudeResponse criar(UnidadeSaudeRequest request) {
        log.info("Criando nova unidade de saúde: {}", request.getNome());

        if (unidadeSaudeRepository.existsByNome(request.getNome())) {
            throw new IllegalArgumentException("Unidade de Saúde com este nome já cadastrada: " + request.getNome());
        }

        UnidadeSaude unidadeSaude = unidadeSaudeMapper.toEntity(request);
        UnidadeSaude unidadeSalva = unidadeSaudeRepository.save(unidadeSaude);
        log.info("Unidade de Saúde criada com sucesso. ID: {}", unidadeSalva.getId());

        return unidadeSaudeMapper.toResponse(unidadeSalva);
    }

    public UnidadeSaudeResponse buscarPorId(Integer id) {
        log.info("Buscando unidade de saúde por ID: {}", id);
        UnidadeSaude unidadeSaude = unidadeSaudeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidade de Saúde não encontrada com ID: " + id));
        return unidadeSaudeMapper.toResponse(unidadeSaude);
    }

    public Page<UnidadeSaudeResponse> listar(Pageable pageable) {
        log.info("Listando unidades de saúde - página: {}, tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        return unidadeSaudeRepository.findAll(pageable)
                .map(unidadeSaudeMapper::toResponse);
    }

    public List<UnidadeSaudeResponse> listarTodos() {
        log.info("Listando todas as unidades de saúde");
        return unidadeSaudeRepository.findAll().stream()
                .map(unidadeSaudeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<UnidadeSaudeResponse> listarComFiltros(String cidade, String bairro) {
        log.info("Listando unidades de saúde - cidade: {}, bairro: {}", cidade, bairro);
        
        List<UnidadeSaude> unidades;
        
        if (cidade != null && !cidade.isBlank() && bairro != null && !bairro.isBlank()) {
            unidades = unidadeSaudeRepository.findByCidadeAndBairro(cidade, bairro);
        } else if (cidade != null && !cidade.isBlank()) {
            unidades = unidadeSaudeRepository.findByCidade(cidade);
        } else if (bairro != null && !bairro.isBlank()) {
            unidades = unidadeSaudeRepository.findByBairro(bairro);
        } else {
            unidades = unidadeSaudeRepository.findAll();
        }
        
        return unidades.stream()
                .map(unidadeSaudeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UnidadeSaudeResponse atualizar(Integer id, UnidadeSaudeRequest request) {
        log.info("Atualizando unidade de saúde ID: {}", id);

        UnidadeSaude unidadeSaude = unidadeSaudeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidade de Saúde não encontrada com ID: " + id));

        if (!unidadeSaude.getNome().equals(request.getNome())) {
            if (unidadeSaudeRepository.existsByNome(request.getNome())) {
                throw new IllegalArgumentException("Unidade de Saúde com este nome já cadastrada: " + request.getNome());
            }
        }

        unidadeSaudeMapper.updateEntityFromRequest(request, unidadeSaude);

        UnidadeSaude unidadeAtualizada = unidadeSaudeRepository.save(unidadeSaude);
        log.info("Unidade de Saúde atualizada com sucesso. ID: {}", unidadeAtualizada.getId());

        return unidadeSaudeMapper.toResponse(unidadeAtualizada);
    }

    @Transactional
    public void deletar(Integer id) {
        log.info("Deletando unidade de saúde ID: {}", id);
        
        UnidadeSaude unidadeSaude = unidadeSaudeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidade de Saúde não encontrada com ID: " + id));
        
        // Verifica se há médicos associados
        if (medicoRepository.existsByUnidadeId(id)) {
            throw new IllegalArgumentException("Não é possível deletar unidade de saúde com médicos associados. ID: " + id);
        }
        
        // Verifica se há consultas associadas
        if (consultaRepository.existsByUnidadeId(id)) {
            throw new IllegalArgumentException("Não é possível deletar unidade de saúde com consultas associadas. ID: " + id);
        }
        
        unidadeSaudeRepository.deleteById(id);
        log.info("Unidade de Saúde deletada com sucesso. ID: {}", id);
    }
}
