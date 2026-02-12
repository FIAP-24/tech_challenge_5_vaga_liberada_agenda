package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.ConsultaRequest;
import com.fiap.vaga_liberada_agenda.dto.response.ConsultaResponse;
import com.fiap.vaga_liberada_agenda.entity.*;
import com.fiap.vaga_liberada_agenda.mapper.ConsultaMapper;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.repository.MedicoRepository;
import com.fiap.vaga_liberada_agenda.repository.PacienteRepository;
import com.fiap.vaga_liberada_agenda.repository.UnidadeSaudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private UnidadeSaudeRepository unidadeSaudeRepository;

    @Mock
    private ConsultaMapper consultaMapper;

    @Mock
    private LiberacaoVagaService liberacaoVagaService;

    @InjectMocks
    private ConsultaService consultaService;

    @Captor
    ArgumentCaptor<Consulta> consultaCaptor;

    private Paciente paciente;
    private Medico medico;
    private UnidadeSaude unidade;

    @BeforeEach
    void setup() {
        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNome("Paciente Teste");

        medico = new Medico();
        medico.setId(2);
        medico.setNome("Medico Teste");
        medico.setAtivo(true);

        unidade = new UnidadeSaude();
        unidade.setId(3);
        unidade.setNome("Unidade Teste");
    }

    @Test
    void agendar_success() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);

        ConsultaRequest request = new ConsultaRequest();
        request.setPacienteId(paciente.getId());
        request.setMedicoId(medico.getId());
        request.setUnidadeId(unidade.getId());
        request.setDataHora(dataHora);
        request.setObservacoes("obs");

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(unidadeSaudeRepository.findById(unidade.getId())).thenReturn(Optional.of(unidade));
        when(consultaRepository.existsConsultaNoHorario(medico.getId(), dataHora)).thenReturn(false);

        Consulta salvo = new Consulta();
        salvo.setId(10);
        salvo.setPaciente(paciente);
        salvo.setMedico(medico);
        salvo.setUnidade(unidade);
        salvo.setDataHora(dataHora);
        salvo.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);

        when(consultaRepository.save(any())).thenReturn(salvo);

        ConsultaResponse resposta = new ConsultaResponse();
        resposta.setId(10);
        when(consultaMapper.toResponse(salvo)).thenReturn(resposta);

        ConsultaResponse result = consultaService.agendar(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10);

        verify(consultaRepository).save(consultaCaptor.capture());
        Consulta capturada = consultaCaptor.getValue();
        assertThat(capturada.getPaciente()).isEqualTo(paciente);
        assertThat(capturada.getMedico()).isEqualTo(medico);
        assertThat(capturada.getUnidade()).isEqualTo(unidade);
        assertThat(capturada.getDataHora()).isEqualTo(dataHora);
        assertThat(capturada.getStatus()).isEqualTo(StatusConsulta.PENDENTE_CONFIRMACAO);
    }

    @Test
    void agendar_paciente_nao_encontrado() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);

        ConsultaRequest request = new ConsultaRequest();
        request.setPacienteId(999);
        request.setMedicoId(medico.getId());
        request.setUnidadeId(unidade.getId());
        request.setDataHora(dataHora);

        when(pacienteRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultaService.agendar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Paciente não encontrado");

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void agendar_medico_inativo() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);

        ConsultaRequest request = new ConsultaRequest();
        request.setPacienteId(paciente.getId());
        request.setMedicoId(medico.getId());
        request.setUnidadeId(unidade.getId());
        request.setDataHora(dataHora);

        medico.setAtivo(false);

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        assertThatThrownBy(() -> consultaService.agendar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Médico não está ativo");

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void confirmarConsulta_prazo_expirado() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);

        Consulta consulta = new Consulta();
        consulta.setId(20);
        consulta.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);
        consulta.setDataHora(dataHora);
        consulta.setDataLimiteConfirmacao(LocalDateTime.now().minusHours(1)); // passado

        when(consultaRepository.findById(20)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.confirmarConsulta(20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prazo para confirmação expirado");

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void desistirConsulta_deve_liberar_vaga() {
        Consulta consulta = new Consulta();
        consulta.setId(30);
        consulta.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);

        when(consultaRepository.findById(30)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = new ConsultaResponse();
        response.setId(30);
        when(consultaMapper.toResponse(any())).thenReturn(response);

        ConsultaResponse result = consultaService.desistirConsulta(30);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(30);

        verify(consultaRepository).save(any());
        verify(liberacaoVagaService).liberarVaga(any());
    }

    @Test
    void agendar_unidade_nao_encontrada() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest();
        request.setPacienteId(paciente.getId());
        request.setMedicoId(medico.getId());
        request.setUnidadeId(999);
        request.setDataHora(dataHora);

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(unidadeSaudeRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultaService.agendar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unidade de saúde não encontrada");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void agendar_conflito_horario() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest();
        request.setPacienteId(paciente.getId());
        request.setMedicoId(medico.getId());
        request.setUnidadeId(unidade.getId());
        request.setDataHora(dataHora);

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(unidadeSaudeRepository.findById(unidade.getId())).thenReturn(Optional.of(unidade));
        when(consultaRepository.existsConsultaNoHorario(medico.getId(), dataHora)).thenReturn(true);

        assertThatThrownBy(() -> consultaService.agendar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma consulta agendada");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void agendar_data_passada() {
        LocalDateTime dataHora = LocalDateTime.now().minusHours(1);
        ConsultaRequest request = new ConsultaRequest();
        request.setPacienteId(paciente.getId());
        request.setMedicoId(medico.getId());
        request.setUnidadeId(unidade.getId());
        request.setDataHora(dataHora);

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(unidadeSaudeRepository.findById(unidade.getId())).thenReturn(Optional.of(unidade));
        when(consultaRepository.existsConsultaNoHorario(medico.getId(), dataHora)).thenReturn(false);

        assertThatThrownBy(() -> consultaService.agendar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data da consulta deve ser futura");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void confirmarConsulta_sucesso() {
        Consulta consulta = new Consulta();
        consulta.setId(20);
        consulta.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);
        consulta.setDataLimiteConfirmacao(LocalDateTime.now().plusHours(1));

        when(consultaRepository.findById(20)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConsultaResponse response = new ConsultaResponse();
        response.setId(20);
        when(consultaMapper.toResponse(any())).thenReturn(response);

        ConsultaResponse result = consultaService.confirmarConsulta(20);

        assertThat(result).isNotNull();
        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.AGENDADA);
        verify(consultaRepository).save(consulta);
    }

    @Test
    void confirmarConsulta_status_invalido() {
        Consulta consulta = new Consulta();
        consulta.setId(20);
        consulta.setStatus(StatusConsulta.AGENDADA);

        when(consultaRepository.findById(20)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.confirmarConsulta(20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pendentes de confirmação");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void cancelarConsulta_sucesso() {
        Consulta consulta = new Consulta();
        consulta.setId(25);
        consulta.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);

        when(consultaRepository.findById(25)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ConsultaResponse response = new ConsultaResponse();
        response.setId(25);
        when(consultaMapper.toResponse(any())).thenReturn(response);

        ConsultaResponse result = consultaService.cancelarConsulta(25);

        assertThat(result).isNotNull();
        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        verify(consultaRepository).save(consulta);
    }

    @Test
    void cancelarConsulta_quandoRealizada_deveLancar() {
        Consulta consulta = new Consulta();
        consulta.setId(25);
        consulta.setStatus(StatusConsulta.REALIZADA);

        when(consultaRepository.findById(25)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.cancelarConsulta(25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já realizada");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void desistirConsulta_quandoRealizada_deveLancar() {
        Consulta consulta = new Consulta();
        consulta.setId(30);
        consulta.setStatus(StatusConsulta.REALIZADA);

        when(consultaRepository.findById(30)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.desistirConsulta(30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já realizada");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void desistirConsulta_quandoCancelada_deveLancar() {
        Consulta consulta = new Consulta();
        consulta.setId(30);
        consulta.setStatus(StatusConsulta.CANCELADA);

        when(consultaRepository.findById(30)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.desistirConsulta(30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cancelada");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarConsulta() {
        Consulta consulta = new Consulta();
        consulta.setId(40);
        when(consultaRepository.findById(40)).thenReturn(Optional.of(consulta));
        ConsultaResponse response = new ConsultaResponse();
        response.setId(40);
        when(consultaMapper.toResponse(consulta)).thenReturn(response);

        ConsultaResponse result = consultaService.buscarPorId(40);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(40);
    }

    @Test
    void buscarPorId_deveLancarQuandoNaoEncontrada() {
        when(consultaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultaService.buscarPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Consulta não encontrada");
    }

    @Test
    void listarPorPaciente_deveRetornarLista() {
        Consulta consulta = new Consulta();
        consulta.setId(1);
        when(consultaRepository.findByPacienteId(1)).thenReturn(java.util.List.of(consulta));
        ConsultaResponse response = new ConsultaResponse();
        when(consultaMapper.toResponse(consulta)).thenReturn(response);

        java.util.List<ConsultaResponse> result = consultaService.listarPorPaciente(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarPorStatus_deveRetornarLista() {
        Consulta consulta = new Consulta();
        when(consultaRepository.findByStatus(StatusConsulta.AGENDADA)).thenReturn(java.util.List.of(consulta));
        ConsultaResponse response = new ConsultaResponse();
        when(consultaMapper.toResponse(consulta)).thenReturn(response);

        java.util.List<ConsultaResponse> result = consultaService.listarPorStatus(StatusConsulta.AGENDADA);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarTodas_deveRetornarLista() {
        Consulta consulta = new Consulta();
        when(consultaRepository.findAll()).thenReturn(java.util.List.of(consulta));
        ConsultaResponse response = new ConsultaResponse();
        when(consultaMapper.toResponse(consulta)).thenReturn(response);

        java.util.List<ConsultaResponse> result = consultaService.listarTodas();

        assertThat(result).hasSize(1);
    }

    @Test
    void verificarConsultasNaoConfirmadas_deveRetornarLista() {
        Consulta consulta = new Consulta();
        consulta.setId(1);
        when(consultaRepository.buscarConsultasNaoConfirmadas(eq(StatusConsulta.PENDENTE_CONFIRMACAO), any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(consulta));

        java.util.List<Consulta> result = consultaService.verificarConsultasNaoConfirmadas();

        assertThat(result).hasSize(1);
    }
}
