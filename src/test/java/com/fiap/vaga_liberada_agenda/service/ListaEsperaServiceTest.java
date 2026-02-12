package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.ListaEsperaRequest;
import com.fiap.vaga_liberada_agenda.dto.response.ListaEsperaResponse;
import com.fiap.vaga_liberada_agenda.entity.*;
import com.fiap.vaga_liberada_agenda.mapper.ListaEsperaMapper;
import com.fiap.vaga_liberada_agenda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaEsperaServiceTest {

    @Mock
    private ListaEsperaRepository listaEsperaRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private EspecialidadeRepository especialidadeRepository;
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private UnidadeSaudeRepository unidadeSaudeRepository;
    @Mock
    private ListaEsperaMapper listaEsperaMapper;

    @InjectMocks
    private ListaEsperaService listaEsperaService;

    private Paciente paciente;
    private Especialidade especialidade;
    private ListaEspera listaEspera;
    private ListaEsperaRequest request;
    private ListaEsperaResponse response;

    @BeforeEach
    void setup() {
        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNome("Paciente Teste");

        especialidade = new Especialidade();
        especialidade.setId(1);
        especialidade.setNome("Clínico Geral");

        listaEspera = new ListaEspera();
        listaEspera.setId(1);
        listaEspera.setPaciente(paciente);
        listaEspera.setEspecialidade(especialidade);
        listaEspera.setStatus(StatusListaEspera.ATIVA);
        listaEspera.setPrioridade(0);

        request = new ListaEsperaRequest();
        request.setPacienteId(1);
        request.setEspecialidadeId(1);
        request.setPrioridade(0);

        response = new ListaEsperaResponse();
        response.setId(1);
        response.setPacienteId(1);
        response.setEspecialidadeId(1);
    }

    @Test
    void adicionarNaLista_deveRetornarListaEsperaCriada() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(listaEsperaRepository.findByPacienteIdAndStatus(1, StatusListaEspera.ATIVA)).thenReturn(List.of());
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenReturn(listaEspera);
        when(listaEsperaMapper.toResponseWithDetails(any(ListaEspera.class))).thenReturn(response);

        ListaEsperaResponse result = listaEsperaService.adicionarNaLista(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(listaEsperaRepository).save(any(ListaEspera.class));
    }

    @Test
    void adicionarNaLista_deveLancarQuandoPacienteNaoEncontrado() {
        when(pacienteRepository.findById(999)).thenReturn(Optional.empty());
        request.setPacienteId(999);

        assertThatThrownBy(() -> listaEsperaService.adicionarNaLista(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Paciente não encontrado");
        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void adicionarNaLista_deveLancarQuandoJaNaListaAtiva() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(listaEsperaRepository.findByPacienteIdAndStatus(1, StatusListaEspera.ATIVA)).thenReturn(List.of(listaEspera));

        assertThatThrownBy(() -> listaEsperaService.adicionarNaLista(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está na lista de espera ativa");
        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void adicionarNaLista_comMedicoEUnidade_deveSalvarComMedicoEUnidade() {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNome("Dr. João");
        UnidadeSaude unidade = new UnidadeSaude();
        unidade.setId(1);
        unidade.setNome("UBS");
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(listaEsperaRepository.findByPacienteIdAndStatus(1, StatusListaEspera.ATIVA)).thenReturn(List.of());
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
        when(unidadeSaudeRepository.findById(1)).thenReturn(Optional.of(unidade));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenReturn(listaEspera);
        when(listaEsperaMapper.toResponseWithDetails(any(ListaEspera.class))).thenReturn(response);
        request.setMedicoId(1);
        request.setUnidadeId(1);

        ListaEsperaResponse result = listaEsperaService.adicionarNaLista(request);

        assertThat(result).isNotNull();
        verify(listaEsperaRepository).save(any(ListaEspera.class));
    }

    @Test
    void adicionarNaLista_deveLancarQuandoMedicoNaoEncontrado() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(listaEsperaRepository.findByPacienteIdAndStatus(1, StatusListaEspera.ATIVA)).thenReturn(List.of());
        when(medicoRepository.findById(999)).thenReturn(Optional.empty());
        request.setMedicoId(999);

        assertThatThrownBy(() -> listaEsperaService.adicionarNaLista(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Médico não encontrado");
        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void adicionarNaLista_deveLancarQuandoUnidadeNaoEncontrada() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(especialidadeRepository.findById(1)).thenReturn(Optional.of(especialidade));
        when(listaEsperaRepository.findByPacienteIdAndStatus(1, StatusListaEspera.ATIVA)).thenReturn(List.of());
        when(unidadeSaudeRepository.findById(999)).thenReturn(Optional.empty());
        request.setUnidadeId(999);

        assertThatThrownBy(() -> listaEsperaService.adicionarNaLista(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unidade de saúde não encontrada");
        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void adicionarNaLista_deveLancarQuandoEspecialidadeNaoEncontrada() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(especialidadeRepository.findById(999)).thenReturn(Optional.empty());
        request.setEspecialidadeId(999);

        assertThatThrownBy(() -> listaEsperaService.adicionarNaLista(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Especialidade não encontrada");
        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void listarPorFiltros_deveRetornarLista() {
        when(listaEsperaRepository.findByFiltros(StatusListaEspera.ATIVA, 1, null, null)).thenReturn(List.of(listaEspera));
        when(listaEsperaMapper.toResponseWithDetails(listaEspera)).thenReturn(response);

        List<ListaEsperaResponse> result = listaEsperaService.listarPorFiltros(1, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void obterProximoDaFila_deveRetornarOptional() {
        when(listaEsperaRepository.findFirstByFiltros(StatusListaEspera.ATIVA, 1, null, null))
                .thenReturn(Optional.of(listaEspera));
        when(listaEsperaMapper.toResponseWithDetails(listaEspera)).thenReturn(response);

        Optional<ListaEsperaResponse> result = listaEsperaService.obterProximoDaFila(1, null, null);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
    }

    @Test
    void obterProximoDaFila_deveRetornarVazioQuandoNaoHaNinguem() {
        when(listaEsperaRepository.findFirstByFiltros(StatusListaEspera.ATIVA, 1, null, null))
                .thenReturn(Optional.empty());

        Optional<ListaEsperaResponse> result = listaEsperaService.obterProximoDaFila(1, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void removerDaLista_deveCancelarRegistro() {
        when(listaEsperaRepository.findById(1)).thenReturn(Optional.of(listaEspera));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenReturn(listaEspera);

        listaEsperaService.removerDaLista(1);

        verify(listaEsperaRepository).save(listaEspera);
        assertThat(listaEspera.getStatus()).isEqualTo(StatusListaEspera.CANCELADA);
    }

    @Test
    void removerDaLista_deveLancarQuandoAguardandoResposta() {
        listaEspera.setStatus(StatusListaEspera.AGUARDANDO_RESPOSTA);
        when(listaEsperaRepository.findById(1)).thenReturn(Optional.of(listaEspera));

        assertThatThrownBy(() -> listaEsperaService.removerDaLista(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aguardando resposta");
        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarListaEspera() {
        when(listaEsperaRepository.findById(1)).thenReturn(Optional.of(listaEspera));
        when(listaEsperaMapper.toResponseWithDetails(listaEspera)).thenReturn(response);

        ListaEsperaResponse result = listaEsperaService.buscarPorId(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void buscarPorId_deveLancarQuandoNaoEncontrado() {
        when(listaEsperaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listaEsperaService.buscarPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado na lista de espera");
    }
}
