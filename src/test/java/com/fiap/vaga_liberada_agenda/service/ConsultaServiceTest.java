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
}
