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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private EspecialidadeRepository especialidadeRepository;
    @Mock
    private UnidadeSaudeRepository unidadeSaudeRepository;
    @Mock
    private MedicoMapper medicoMapper;
    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private MedicoService medicoService;

    private Medico medico;
    private Especialidade especialidade;
    private UnidadeSaude unidade;
    private MedicoRequest request;
    private MedicoResponse response;

    @BeforeEach
    void setup() {
        especialidade = new Especialidade();
        especialidade.setId(1);
        especialidade.setNome("Clínico Geral");

        unidade = new UnidadeSaude();
        unidade.setId(1);
        unidade.setNome("UBS Centro");

        medico = new Medico();
        medico.setId(1);
        medico.setNome("Dr. João");
        medico.setCrm("12345");
        medico.setAtivo(true);
        medico.setEspecialidade(especialidade);
        medico.setUnidade(unidade);

        request = new MedicoRequest();
        request.setNome("Dr. João");
        request.setCrm("12345");
        request.setEspecialidadeId(1);
        request.setUnidadeId(1);
        request.setAtivo(true);

        response = new MedicoResponse();
        response.setId(1);
        response.setNome("Dr. João");
    }

    @Test
    void criar_deveRetornarMedicoCriado() {
        when(medicoRepository.existsByCrm(request.getCrm())).thenReturn(false);
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(medicoMapper.toEntity(request)).thenReturn(medico);
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        MedicoResponse result = medicoService.criar(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(medicoRepository).save(any(Medico.class));
    }

    @Test
    void criar_comAtivoNulo_deveDefinirAtivoTrue() {
        request.setAtivo(null);
        Medico medicoSemAtivo = new Medico();
        medicoSemAtivo.setId(1);
        medicoSemAtivo.setNome("Dr. João");
        medicoSemAtivo.setCrm("12345");
        medicoSemAtivo.setEspecialidade(especialidade);
        medicoSemAtivo.setUnidade(unidade);
        when(medicoRepository.existsByCrm(request.getCrm())).thenReturn(false);
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(medicoMapper.toEntity(request)).thenReturn(medicoSemAtivo);
        when(medicoRepository.save(any(Medico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(medicoMapper.toResponse(any(Medico.class))).thenReturn(response);

        medicoService.criar(request);

        assertThat(medicoSemAtivo.getAtivo()).isTrue();
    }

    @Test
    void criar_deveLancarQuandoCrmJaCadastrado() {
        when(medicoRepository.existsByCrm(request.getCrm())).thenReturn(true);

        assertThatThrownBy(() -> medicoService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CRM já cadastrado");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void criar_deveLancarQuandoEspecialidadeNaoEncontrada() {
        when(medicoRepository.existsByCrm(request.getCrm())).thenReturn(false);
        when(especialidadeRepository.findById(999)).thenReturn(Optional.empty());
        request.setEspecialidadeId(999);

        assertThatThrownBy(() -> medicoService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Especialidade não encontrada");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarMedico() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        MedicoResponse result = medicoService.buscarPorId(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void buscarPorId_deveLancarQuandoNaoEncontrado() {
        when(medicoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicoService.buscarPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Médico não encontrado");
    }

    @Test
    void buscarPorCrm_deveRetornarMedico() {
        when(medicoRepository.findByCrm("12345")).thenReturn(Optional.of(medico));
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        MedicoResponse result = medicoService.buscarPorCrm("12345");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void buscarPorCrm_deveLancarQuandoNaoEncontrado() {
        when(medicoRepository.findByCrm("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicoService.buscarPorCrm("99999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Médico não encontrado com CRM");
    }

    @Test
    void listarComFiltros_deveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findAll(pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void listarComFiltros_especialidadeUnidadeAtivo_deveChamarRepositorioCorreto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findByEspecialidadeIdAndUnidadeIdAndAtivo(1, 1, true, pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, 1, 1, true);

        assertThat(result.getContent()).hasSize(1);
        verify(medicoRepository).findByEspecialidadeIdAndUnidadeIdAndAtivo(1, 1, true, pageable);
    }

    @Test
    void listarComFiltros_especialidadeUnidade_deveChamarRepositorioCorreto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findByEspecialidadeIdAndUnidadeId(1, 1, pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, 1, 1, null);

        assertThat(result.getContent()).hasSize(1);
        verify(medicoRepository).findByEspecialidadeIdAndUnidadeId(1, 1, pageable);
    }

    @Test
    void listarComFiltros_apenasEspecialidade_deveChamarRepositorioCorreto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findByEspecialidadeId(1, pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, 1, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(medicoRepository).findByEspecialidadeId(1, pageable);
    }

    @Test
    void listarComFiltros_apenasUnidade_deveChamarRepositorioCorreto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findByUnidadeId(1, pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, null, 1, null);

        assertThat(result.getContent()).hasSize(1);
        verify(medicoRepository).findByUnidadeId(1, pageable);
    }

    @Test
    void listarComFiltros_apenasAtivo_deveChamarRepositorioCorreto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findByAtivo(true, pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, null, null, true);

        assertThat(result.getContent()).hasSize(1);
        verify(medicoRepository).findByAtivo(true, pageable);
    }

    @Test
    void listarComFiltros_especialidadeAtivo_deveChamarRepositorioCorreto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findByEspecialidadeIdAndAtivo(1, true, pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, 1, null, true);

        assertThat(result.getContent()).hasSize(1);
        verify(medicoRepository).findByEspecialidadeIdAndAtivo(1, true, pageable);
    }

    @Test
    void listarComFiltros_unidadeAtivo_deveChamarRepositorioCorreto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Medico> page = new PageImpl<>(List.of(medico));
        when(medicoRepository.findByUnidadeIdAndAtivo(1, true, pageable)).thenReturn(page);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        Page<MedicoResponse> result = medicoService.listarComFiltros(pageable, null, 1, true);

        assertThat(result.getContent()).hasSize(1);
        verify(medicoRepository).findByUnidadeIdAndAtivo(1, true, pageable);
    }

    @Test
    void criar_deveLancarQuandoUnidadeNaoEncontrada() {
        when(medicoRepository.existsByCrm(request.getCrm())).thenReturn(false);
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(unidadeSaudeRepository.findById(999)).thenReturn(Optional.empty());
        request.setUnidadeId(999);

        assertThatThrownBy(() -> medicoService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unidade de Saúde não encontrada");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void atualizar_deveLancarQuandoCrmAlteradoEDuplicado() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        when(medicoRepository.existsByCrm("99999")).thenReturn(true);
        request.setCrm("99999");

        assertThatThrownBy(() -> medicoService.atualizar(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CRM já cadastrado");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void atualizar_deveLancarQuandoEspecialidadeNaoEncontrada() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        lenient().when(medicoRepository.existsByCrm(any())).thenReturn(false);
        when(especialidadeRepository.findById(999)).thenReturn(Optional.empty());
        request.setEspecialidadeId(999);

        assertThatThrownBy(() -> medicoService.atualizar(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Especialidade não encontrada");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void atualizar_deveLancarQuandoUnidadeNaoEncontrada() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        lenient().when(medicoRepository.existsByCrm(any())).thenReturn(false);
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(unidadeSaudeRepository.findById(999)).thenReturn(Optional.empty());
        request.setUnidadeId(999);

        assertThatThrownBy(() -> medicoService.atualizar(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unidade de Saúde não encontrada");
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void atualizar_deveRetornarMedicoAtualizado() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        lenient().when(medicoRepository.existsByCrm(any())).thenReturn(false);
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);
        when(medicoMapper.toResponse(medico)).thenReturn(response);

        MedicoResponse result = medicoService.atualizar(1, request);

        assertThat(result).isNotNull();
        verify(medicoRepository).save(medico);
    }

    @Test
    void desativar_deveAlterarStatus() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);

        medicoService.desativar(1);

        verify(medicoRepository).save(medico);
        assertThat(medico.getAtivo()).isFalse();
    }

    @Test
    void ativar_deveAlterarStatus() {
        medico.setAtivo(false);
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);

        medicoService.ativar(1);

        verify(medicoRepository).save(medico);
        assertThat(medico.getAtivo()).isTrue();
    }

    @Test
    void deletar_deveRemoverMedico() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        when(consultaRepository.existsByMedicoId(1)).thenReturn(false);

        medicoService.deletar(1);

        verify(medicoRepository).deleteById(1);
    }

    @Test
    void deletar_deveLancarQuandoTemConsultas() {
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        when(consultaRepository.existsByMedicoId(1)).thenReturn(true);

        assertThatThrownBy(() -> medicoService.deletar(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("consultas associadas");
        verify(medicoRepository, never()).deleteById(any());
    }
}
