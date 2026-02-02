package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.entity.*;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.repository.ListaEsperaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiberacaoVagaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private ListaEsperaRepository listaEsperaRepository;
    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private LiberacaoVagaService liberacaoVagaService;

    private Consulta consulta;
    private Medico medico;
    private Especialidade especialidade;
    private UnidadeSaude unidade;
    private Paciente paciente;
    private ListaEspera listaEspera;

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
        medico.setEspecialidade(especialidade);
        medico.setUnidade(unidade);

        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNome("Paciente Teste");

        consulta = new Consulta();
        consulta.setId(10);
        consulta.setPaciente(paciente);
        consulta.setMedico(medico);
        consulta.setUnidade(unidade);
        consulta.setDataHora(LocalDateTime.now().plusDays(1));
        consulta.setStatus(StatusConsulta.PENDENTE_CONFIRMACAO);

        listaEspera = new ListaEspera();
        listaEspera.setId(5);
        listaEspera.setPaciente(paciente);
        listaEspera.setEspecialidade(especialidade);
        listaEspera.setStatus(StatusListaEspera.ATIVA);
    }

    @Test
    void liberarVaga_deveMarcarConsultaComoLiberada() {
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), eq(1), eq(1)))
                .thenReturn(Optional.empty());
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), isNull(), isNull()))
                .thenReturn(Optional.empty());
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.liberarVaga(consulta);

        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.LIBERADA);
        verify(consultaRepository).save(consulta);
    }

    @Test
    void liberarVaga_quandoHaProximoNaFila_deveOferecerVaga() {
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), eq(1), eq(1)))
                .thenReturn(Optional.of(listaEspera));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.liberarVaga(consulta);

        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.LIBERADA);
        verify(notificacaoService).enviar(any(String.class));
        verify(listaEsperaRepository).save(listaEspera);
        assertThat(listaEspera.getStatus()).isEqualTo(StatusListaEspera.AGUARDANDO_RESPOSTA);
    }

    @Test
    void liberarVaga_quandoProximoSoPorEspecialidade_deveOferecerVaga() {
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), eq(1), eq(1)))
                .thenReturn(Optional.empty());
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), isNull(), isNull()))
                .thenReturn(Optional.of(listaEspera));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.liberarVaga(consulta);

        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.LIBERADA);
        verify(notificacaoService).enviar(any(String.class));
        verify(listaEsperaRepository).save(listaEspera);
    }

    @Test
    void oferecerVagaParaListaEspera_deveAtualizarEEnviarNotificacao() {
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.oferecerVagaParaListaEspera(consulta, listaEspera);

        assertThat(listaEspera.getStatus()).isEqualTo(StatusListaEspera.AGUARDANDO_RESPOSTA);
        assertThat(listaEspera.getConsultaOferecida()).isEqualTo(consulta);
        verify(notificacaoService).enviar(any(String.class));
    }

    @Test
    void aceitarVaga_deveReatribuirConsulta() {
        consulta.setStatus(StatusConsulta.LIBERADA);
        consulta.setVagaOferecidaParaListaEspera(listaEspera);
        listaEspera.setStatus(StatusListaEspera.AGUARDANDO_RESPOSTA);
        listaEspera.setConsultaOferecida(consulta);

        when(consultaRepository.findById(10)).thenReturn(Optional.of(consulta));
        when(listaEsperaRepository.findById(5)).thenReturn(Optional.of(listaEspera));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.aceitarVaga(10, 5);

        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(consulta.getPaciente()).isEqualTo(paciente);
        assertThat(listaEspera.getStatus()).isEqualTo(StatusListaEspera.ATENDIDA);
    }

    @Test
    void aceitarVaga_deveLancarQuandoConsultaNaoEncontrada() {
        when(consultaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> liberacaoVagaService.aceitarVaga(999, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Consulta não encontrada");
    }

    @Test
    void aceitarVaga_deveLancarQuandoVagaNaoDisponivel() {
        consulta.setStatus(StatusConsulta.AGENDADA); // não está mais liberada
        when(consultaRepository.findById(10)).thenReturn(Optional.of(consulta));
        when(listaEsperaRepository.findById(5)).thenReturn(Optional.of(listaEspera));

        assertThatThrownBy(() -> liberacaoVagaService.aceitarVaga(10, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não está mais disponível");
    }

    @Test
    void processarTimeoutVaga_quandoSemOferta_naoFazNada() {
        consulta.setVagaOferecidaParaListaEspera(null);

        liberacaoVagaService.processarTimeoutVaga(consulta);

        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void processarTimeoutVaga_quandoDentroDoPrazo_naoFazNada() {
        listaEspera.setDataOferta(LocalDateTime.now().minusMinutes(30)); // 30 min atrás
        consulta.setVagaOferecidaParaListaEspera(listaEspera);

        liberacaoVagaService.processarTimeoutVaga(consulta);

        verify(listaEsperaRepository, never()).save(any());
    }

    @Test
    void processarTimeoutVaga_quandoExpiradoEHaProximoComIdDiferente_deveOferecerParaProximo() {
        listaEspera.setDataOferta(LocalDateTime.now().minusHours(3));
        consulta.setVagaOferecidaParaListaEspera(listaEspera);

        ListaEspera proximoNaFila = new ListaEspera();
        proximoNaFila.setId(99);
        proximoNaFila.setPaciente(paciente);
        proximoNaFila.setEspecialidade(especialidade);
        proximoNaFila.setStatus(StatusListaEspera.ATIVA);

        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), eq(1), eq(1)))
                .thenReturn(Optional.of(proximoNaFila));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.processarTimeoutVaga(consulta);

        verify(listaEsperaRepository, times(2)).save(any(ListaEspera.class));
        verify(consultaRepository, times(2)).save(consulta);
        assertThat(listaEspera.getStatus()).isEqualTo(StatusListaEspera.ATIVA);
        assertThat(listaEspera.getConsultaOferecida()).isNull();
        verify(notificacaoService).enviar(any(String.class));
        assertThat(proximoNaFila.getStatus()).isEqualTo(StatusListaEspera.AGUARDANDO_RESPOSTA);
    }

    @Test
    void processarTimeoutVaga_quandoExpiradoSemProximo_naoOferece() {
        listaEspera.setDataOferta(LocalDateTime.now().minusHours(3));
        consulta.setVagaOferecidaParaListaEspera(listaEspera);

        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), eq(1), eq(1)))
                .thenReturn(Optional.empty());
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), isNull(), isNull()))
                .thenReturn(Optional.empty());
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.processarTimeoutVaga(consulta);

        verify(listaEsperaRepository).save(listaEspera);
        verify(consultaRepository).save(consulta);
        verify(notificacaoService, never()).enviar(any());
    }

    @Test
    void processarTimeoutVaga_quandoExpiradoProximoEhMesmoRegistro_naoOfereceNovamente() {
        listaEspera.setDataOferta(LocalDateTime.now().minusHours(3));
        consulta.setVagaOferecidaParaListaEspera(listaEspera);
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), eq(1), eq(1)))
                .thenReturn(Optional.of(listaEspera));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.processarTimeoutVaga(consulta);

        verify(listaEsperaRepository).save(listaEspera);
        verify(notificacaoService, never()).enviar(any());
    }

    @Test
    void processarTimeoutVaga_quandoExpiradoProximoSoNaBuscaPorEspecialidade_deveOferecer() {
        listaEspera.setDataOferta(LocalDateTime.now().minusHours(3));
        consulta.setVagaOferecidaParaListaEspera(listaEspera);
        ListaEspera proximoSoEspecialidade = new ListaEspera();
        proximoSoEspecialidade.setId(88);
        proximoSoEspecialidade.setPaciente(paciente);
        proximoSoEspecialidade.setEspecialidade(especialidade);
        proximoSoEspecialidade.setStatus(StatusListaEspera.ATIVA);

        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), eq(1), eq(1)))
                .thenReturn(Optional.empty());
        when(listaEsperaRepository.findFirstByFiltros(eq(StatusListaEspera.ATIVA), eq(1), isNull(), isNull()))
                .thenReturn(Optional.of(proximoSoEspecialidade));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(inv -> inv.getArgument(0));

        liberacaoVagaService.processarTimeoutVaga(consulta);

        verify(notificacaoService).enviar(any(String.class));
        assertThat(proximoSoEspecialidade.getStatus()).isEqualTo(StatusListaEspera.AGUARDANDO_RESPOSTA);
    }
}
