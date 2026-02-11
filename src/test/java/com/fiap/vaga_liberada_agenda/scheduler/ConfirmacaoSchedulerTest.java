package com.fiap.vaga_liberada_agenda.scheduler;

import com.fiap.vaga_liberada_agenda.entity.Consulta;
import com.fiap.vaga_liberada_agenda.service.ConsultaService;
import com.fiap.vaga_liberada_agenda.service.LiberacaoVagaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmacaoSchedulerTest {

    @Mock
    private ConsultaService consultaService;

    @Mock
    private LiberacaoVagaService liberacaoVagaService;

    @InjectMocks
    private ConfirmacaoScheduler confirmacaoScheduler;

    @Test
    void verificarConsultasNaoConfirmadas_quandoVazio_naoLiberaVaga() {
        when(consultaService.verificarConsultasNaoConfirmadas()).thenReturn(List.of());

        confirmacaoScheduler.verificarConsultasNaoConfirmadas();

        verify(consultaService).verificarConsultasNaoConfirmadas();
        verify(liberacaoVagaService, never()).liberarVaga(any());
    }

    @Test
    void verificarConsultasNaoConfirmadas_quandoHaConsultas_liberaVagaParaCadaUma() {
        Consulta consulta = new Consulta();
        consulta.setId(1);
        when(consultaService.verificarConsultasNaoConfirmadas()).thenReturn(List.of(consulta));

        confirmacaoScheduler.verificarConsultasNaoConfirmadas();

        verify(consultaService).verificarConsultasNaoConfirmadas();
        verify(liberacaoVagaService).liberarVaga(consulta);
    }

    @Test
    void verificarConsultasNaoConfirmadas_quandoExcecao_naoPropaga() {
        when(consultaService.verificarConsultasNaoConfirmadas()).thenThrow(new RuntimeException("Erro"));

        confirmacaoScheduler.verificarConsultasNaoConfirmadas();

        verify(consultaService).verificarConsultasNaoConfirmadas();
        verify(liberacaoVagaService, never()).liberarVaga(any());
    }
}
