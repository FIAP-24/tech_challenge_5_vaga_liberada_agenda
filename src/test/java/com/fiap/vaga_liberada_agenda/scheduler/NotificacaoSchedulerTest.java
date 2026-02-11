package com.fiap.vaga_liberada_agenda.scheduler;

import com.fiap.vaga_liberada_agenda.entity.Consulta;
import com.fiap.vaga_liberada_agenda.entity.Medico;
import com.fiap.vaga_liberada_agenda.entity.Paciente;
import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import com.fiap.vaga_liberada_agenda.entity.UnidadeSaude;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.service.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoSchedulerTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private NotificacaoScheduler notificacaoScheduler;

    private Consulta consulta;
    private Paciente paciente;
    private UnidadeSaude unidade;

    @BeforeEach
    void setup() {
        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNome("Paciente Teste");

        unidade = new UnidadeSaude();
        unidade.setId(1);
        unidade.setNome("UBS Centro");

        Medico medico = new Medico();
        medico.setId(1);
        medico.setNome("Dr. João");

        consulta = new Consulta();
        consulta.setId(10);
        consulta.setPaciente(paciente);
        consulta.setUnidade(unidade);
        consulta.setMedico(medico);
        consulta.setDataHora(LocalDateTime.now().plusHours(12));
        consulta.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);
        consulta.setLembreteEnviado(false);
    }

    @Test
    void verificarConsultasProximas_quandoVazio_naoEnviaNotificacao() {
        when(consultaRepository.buscarConsultasParaNotificar(any(), any(), eq(StatusConsulta.AGENDADA)))
                .thenReturn(List.of());
        when(consultaRepository.buscarConsultasParaNotificar(any(), any(), eq(StatusConsulta.PENDENTE_CONFIRMACAO)))
                .thenReturn(List.of());

        notificacaoScheduler.verificarConsultasProximas();

        verify(notificacaoService, never()).enviar(any());
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void verificarConsultasProximas_quandoHaConsultas_enviaNotificacaoEMarcaEnviado() {
        when(consultaRepository.buscarConsultasParaNotificar(any(), any(), eq(StatusConsulta.AGENDADA)))
                .thenReturn(List.of());
        when(consultaRepository.buscarConsultasParaNotificar(any(), any(), eq(StatusConsulta.PENDENTE_CONFIRMACAO)))
                .thenReturn(List.of(consulta));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        notificacaoScheduler.verificarConsultasProximas();

        verify(notificacaoService).enviar(any(String.class));
        verify(consultaRepository).save(consulta);
        assertThat(consulta.getLembreteEnviado()).isTrue();
    }

    @Test
    void verificarConsultasProximas_quandoHaConsultasAgendadas_enviaNotificacao() {
        consulta.setStatus(StatusConsulta.AGENDADA);
        when(consultaRepository.buscarConsultasParaNotificar(any(), any(), eq(StatusConsulta.AGENDADA)))
                .thenReturn(List.of(consulta));
        when(consultaRepository.buscarConsultasParaNotificar(any(), any(), eq(StatusConsulta.PENDENTE_CONFIRMACAO)))
                .thenReturn(List.of());
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        notificacaoScheduler.verificarConsultasProximas();

        verify(notificacaoService).enviar(any(String.class));
        verify(consultaRepository).save(consulta);
    }
}
