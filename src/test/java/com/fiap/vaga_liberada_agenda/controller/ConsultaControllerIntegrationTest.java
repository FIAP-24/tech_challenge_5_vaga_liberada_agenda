package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.response.ConsultaResponse;
import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import com.fiap.vaga_liberada_agenda.exception.GlobalExceptionHandler;
import com.fiap.vaga_liberada_agenda.service.ConsultaService;
import com.fiap.vaga_liberada_agenda.service.LiberacaoVagaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ConsultaController.class)
@Import(GlobalExceptionHandler.class)
class ConsultaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultaService consultaService;

    @MockBean
    private LiberacaoVagaService liberacaoVagaService;

    @Test
    void agendar_deveRetornar201() throws Exception {
        String body = "{\"pacienteId\":1,\"medicoId\":1,\"unidadeId\":1,\"dataHora\":\"2026-12-01T10:00:00\"}";
        ConsultaResponse response = new ConsultaResponse();
        response.setId(1);
        response.setPacienteId(1);
        response.setMedicoId(1);

        when(consultaService.agendar(any())).thenReturn(response);

        mockMvc.perform(post("/api/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void buscarPorId_deveRetornar200() throws Exception {
        ConsultaResponse response = new ConsultaResponse();
        response.setId(1);
        when(consultaService.buscarPorId(1)).thenReturn(response);

        mockMvc.perform(get("/api/consultas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void listar_semFiltros_deveRetornar200() throws Exception {
        ConsultaResponse response = new ConsultaResponse();
        response.setId(1);
        when(consultaService.listarTodas()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/consultas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listar_porPaciente_deveRetornar200() throws Exception {
        ConsultaResponse response = new ConsultaResponse();
        response.setId(1);
        response.setPacienteId(1);
        when(consultaService.listarPorPaciente(1)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/consultas").param("pacienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void confirmar_deveRetornar200() throws Exception {
        ConsultaResponse response = new ConsultaResponse();
        response.setId(1);
        response.setStatus(StatusConsulta.AGENDADA);
        when(consultaService.confirmarConsulta(1)).thenReturn(response);

        mockMvc.perform(post("/api/consultas/1/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    void cancelar_deveRetornar200() throws Exception {
        ConsultaResponse response = new ConsultaResponse();
        response.setId(1);
        response.setStatus(StatusConsulta.CANCELADA);
        when(consultaService.cancelarConsulta(1)).thenReturn(response);

        mockMvc.perform(patch("/api/consultas/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void desistir_deveRetornar200() throws Exception {
        ConsultaResponse response = new ConsultaResponse();
        response.setId(1);
        when(consultaService.desistirConsulta(1)).thenReturn(response);

        mockMvc.perform(patch("/api/consultas/1/desistir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
