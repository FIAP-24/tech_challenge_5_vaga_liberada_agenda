package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.UnidadeSaudeRequest;
import com.fiap.vaga_liberada_agenda.dto.response.UnidadeSaudeResponse;
import com.fiap.vaga_liberada_agenda.entity.UnidadeSaude;
import com.fiap.vaga_liberada_agenda.mapper.UnidadeSaudeMapper;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.repository.MedicoRepository;
import com.fiap.vaga_liberada_agenda.repository.UnidadeSaudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UnidadeSaudeServiceTest {

    @Mock
    private UnidadeSaudeRepository unidadeSaudeRepository;
    @Mock
    private UnidadeSaudeMapper unidadeSaudeMapper;
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private UnidadeSaudeService unidadeSaudeService;

    private UnidadeSaude unidade;
    private UnidadeSaudeRequest request;
    private UnidadeSaudeResponse response;

    @BeforeEach
    void setup() {
        unidade = new UnidadeSaude();
        unidade.setId(1);
        unidade.setNome("UBS Centro");
        unidade.setCidade("São Paulo");
        unidade.setBairro("Centro");

        request = new UnidadeSaudeRequest();
        request.setNome("UBS Centro");
        request.setCidade("São Paulo");
        request.setBairro("Centro");
        request.setLatitude(BigDecimal.valueOf(-23.55));
        request.setLongitude(BigDecimal.valueOf(-46.63));

        response = new UnidadeSaudeResponse();
        response.setId(1);
        response.setNome("UBS Centro");
    }

    @Test
    void criar_deveRetornarUnidadeCriada() {
        when(unidadeSaudeRepository.existsByNome(request.getNome())).thenReturn(false);
        when(unidadeSaudeMapper.toEntity(request)).thenReturn(unidade);
        when(unidadeSaudeRepository.save(any(UnidadeSaude.class))).thenReturn(unidade);
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        UnidadeSaudeResponse result = unidadeSaudeService.criar(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(unidadeSaudeRepository).save(any(UnidadeSaude.class));
    }

    @Test
    void criar_deveLancarQuandoNomeJaCadastrado() {
        when(unidadeSaudeRepository.existsByNome(request.getNome())).thenReturn(true);

        assertThatThrownBy(() -> unidadeSaudeService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já cadastrada");
        verify(unidadeSaudeRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarUnidade() {
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        UnidadeSaudeResponse result = unidadeSaudeService.buscarPorId(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void buscarPorId_deveLancarQuandoNaoEncontrada() {
        when(unidadeSaudeRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unidadeSaudeService.buscarPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unidade de Saúde não encontrada");
    }

    @Test
    void listar_deveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UnidadeSaude> page = new PageImpl<>(List.of(unidade));
        when(unidadeSaudeRepository.findAll(pageable)).thenReturn(page);
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        Page<UnidadeSaudeResponse> result = unidadeSaudeService.listar(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void listarComFiltros_comCidadeEBairro_deveRetornarFiltrado() {
        when(unidadeSaudeRepository.findByCidadeAndBairro("São Paulo", "Centro")).thenReturn(List.of(unidade));
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        List<UnidadeSaudeResponse> result = unidadeSaudeService.listarComFiltros("São Paulo", "Centro");

        assertThat(result).hasSize(1);
    }

    @Test
    void listarComFiltros_apenasCidade_deveRetornarFiltrado() {
        when(unidadeSaudeRepository.findByCidade("São Paulo")).thenReturn(List.of(unidade));
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        List<UnidadeSaudeResponse> result = unidadeSaudeService.listarComFiltros("São Paulo", null);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarComFiltros_apenasBairro_deveRetornarFiltrado() {
        when(unidadeSaudeRepository.findByBairro("Centro")).thenReturn(List.of(unidade));
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        List<UnidadeSaudeResponse> result = unidadeSaudeService.listarComFiltros(null, "Centro");

        assertThat(result).hasSize(1);
        verify(unidadeSaudeRepository).findByBairro("Centro");
    }

    @Test
    void listarComFiltros_semFiltros_deveRetornarTodos() {
        when(unidadeSaudeRepository.findAll()).thenReturn(List.of(unidade));
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        List<UnidadeSaudeResponse> result = unidadeSaudeService.listarComFiltros(null, null);

        assertThat(result).hasSize(1);
        verify(unidadeSaudeRepository).findAll();
    }

    @Test
    void listarComFiltros_cidadeEBairroEmBranco_deveRetornarTodos() {
        when(unidadeSaudeRepository.findAll()).thenReturn(List.of(unidade));
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        List<UnidadeSaudeResponse> result = unidadeSaudeService.listarComFiltros("", "");

        assertThat(result).hasSize(1);
        verify(unidadeSaudeRepository).findAll();
    }

    @Test
    void atualizar_deveRetornarUnidadeAtualizada() {
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        lenient().when(unidadeSaudeRepository.existsByNome(any())).thenReturn(false);
        when(unidadeSaudeRepository.save(any(UnidadeSaude.class))).thenReturn(unidade);
        when(unidadeSaudeMapper.toResponse(unidade)).thenReturn(response);

        UnidadeSaudeResponse result = unidadeSaudeService.atualizar(1, request);

        assertThat(result).isNotNull();
        verify(unidadeSaudeRepository).save(unidade);
    }

    @Test
    void atualizar_deveLancarQuandoNomeAlteradoEDuplicado() {
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(unidadeSaudeRepository.existsByNome("Outro Nome")).thenReturn(true);
        request.setNome("Outro Nome");

        assertThatThrownBy(() -> unidadeSaudeService.atualizar(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já cadastrada");
        verify(unidadeSaudeRepository, never()).save(any());
    }

    @Test
    void deletar_deveRemoverUnidade() {
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(medicoRepository.existsByUnidadeId(1)).thenReturn(false);
        when(consultaRepository.existsByUnidadeId(1)).thenReturn(false);

        unidadeSaudeService.deletar(1);

        verify(unidadeSaudeRepository).deleteById(1);
    }

    @Test
    void deletar_deveLancarQuandoTemMedicos() {
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(medicoRepository.existsByUnidadeId(1)).thenReturn(true);

        assertThatThrownBy(() -> unidadeSaudeService.deletar(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("médicos associados");
        verify(unidadeSaudeRepository, never()).deleteById(any());
    }

    @Test
    void deletar_deveLancarQuandoTemConsultas() {
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(medicoRepository.existsByUnidadeId(1)).thenReturn(false);
        when(consultaRepository.existsByUnidadeId(1)).thenReturn(true);

        assertThatThrownBy(() -> unidadeSaudeService.deletar(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("consultas associadas");
        verify(unidadeSaudeRepository, never()).deleteById(any());
    }
}
