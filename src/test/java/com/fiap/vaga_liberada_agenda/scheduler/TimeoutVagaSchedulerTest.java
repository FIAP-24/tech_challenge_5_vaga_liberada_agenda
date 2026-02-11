package com.fiap.vaga_liberada_agenda.scheduler;

import com.fiap.vaga_liberada_agenda.entity.Consulta;
import com.fiap.vaga_liberada_agenda.entity.ListaEspera;
import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.service.LiberacaoVagaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeoutVagaSchedulerTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private LiberacaoVagaService liberacaoVagaService;

    @InjectMocks
    private TimeoutVagaScheduler timeoutVagaScheduler;

    @Test
    void processarTimeoutsDeVagas_quandoNenhumaLiberada_naoProcessa() {
        when(consultaRepository.findByStatus(StatusConsulta.LIBERADA)).thenReturn(List.of());

        timeoutVagaScheduler.processarTimeoutsDeVagas();

        verify(consultaRepository).findByStatus(StatusConsulta.LIBERADA);
        verify(liberacaoVagaService, never()).processarTimeoutVaga(any());
    }

    @Test
    void processarTimeoutsDeVagas_quandoLiberadaSemOferta_naoProcessa() {
        Consulta consulta = new Consulta();
        consulta.setId(1);
        consulta.setStatus(StatusConsulta.LIBERADA);
        consulta.setVagaOferecidaParaListaEspera(null);
        when(consultaRepository.findByStatus(StatusConsulta.LIBERADA)).thenReturn(List.of(consulta));

        timeoutVagaScheduler.processarTimeoutsDeVagas();

        verify(consultaRepository).findByStatus(StatusConsulta.LIBERADA);
        verify(liberacaoVagaService, never()).processarTimeoutVaga(any());
    }

    @Test
    void processarTimeoutsDeVagas_quandoLiberadaComOferta_processaTimeout() {
        Consulta consulta = new Consulta();
        consulta.setId(1);
        consulta.setStatus(StatusConsulta.LIBERADA);
        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setId(5);
        consulta.setVagaOferecidaParaListaEspera(listaEspera);
        when(consultaRepository.findByStatus(StatusConsulta.LIBERADA)).thenReturn(List.of(consulta));

        timeoutVagaScheduler.processarTimeoutsDeVagas();

        verify(consultaRepository).findByStatus(StatusConsulta.LIBERADA);
        verify(liberacaoVagaService).processarTimeoutVaga(consulta);
    }

    @Test
    void processarTimeoutsDeVagas_quandoExcecao_naoPropaga() {
        when(consultaRepository.findByStatus(StatusConsulta.LIBERADA)).thenThrow(new RuntimeException("Erro"));

        timeoutVagaScheduler.processarTimeoutsDeVagas();

        verify(consultaRepository).findByStatus(StatusConsulta.LIBERADA);
        verify(liberacaoVagaService, never()).processarTimeoutVaga(any());
    }
}
