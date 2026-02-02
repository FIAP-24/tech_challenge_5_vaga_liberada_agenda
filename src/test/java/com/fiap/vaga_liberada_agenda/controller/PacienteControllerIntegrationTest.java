package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.response.PacienteResponse;
import com.fiap.vaga_liberada_agenda.exception.GlobalExceptionHandler;
import com.fiap.vaga_liberada_agenda.service.PacienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PacienteController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class PacienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacienteService pacienteService;

    @Test
    void criar_deveRetornar201() throws Exception {
        String body = "{\"nome\":\"João\",\"cpf\":\"12345678901\",\"email\":\"joao@email.com\"}";
        PacienteResponse response = new PacienteResponse();
        response.setId(1);
        response.setNome("João");
        response.setCpf("12345678901");

        when(pacienteService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    void buscarPorId_deveRetornar200() throws Exception {
        PacienteResponse response = new PacienteResponse();
        response.setId(1);
        response.setNome("João");
        when(pacienteService.buscarPorId(1)).thenReturn(response);

        mockMvc.perform(get("/api/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"));
    }

    @Test
    void buscarPorId_quandoNaoEncontrado_deveRetornar400() throws Exception {
        when(pacienteService.buscarPorId(999)).thenThrow(new IllegalArgumentException("Paciente não encontrado com ID: 999"));

        mockMvc.perform(get("/api/pacientes/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Paciente não encontrado com ID: 999"));
    }

    @Test
    void listar_deveRetornar200() throws Exception {
        PacienteResponse response = new PacienteResponse();
        response.setId(1);
        response.setNome("João");
        when(pacienteService.listar(any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void atualizar_deveRetornar200() throws Exception {
        String body = "{\"nome\":\"João Atualizado\",\"cpf\":\"12345678901\"}";
        PacienteResponse response = new PacienteResponse();
        response.setId(1);
        response.setNome("João Atualizado");
        when(pacienteService.atualizar(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Atualizado"));
    }

    @Test
    void deletar_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/pacientes/1"))
                .andExpect(status().isNoContent());
    }
}
