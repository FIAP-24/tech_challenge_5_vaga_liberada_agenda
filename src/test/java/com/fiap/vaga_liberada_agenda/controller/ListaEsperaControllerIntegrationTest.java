package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.response.ListaEsperaResponse;
import com.fiap.vaga_liberada_agenda.exception.GlobalExceptionHandler;
import com.fiap.vaga_liberada_agenda.service.ListaEsperaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ListaEsperaController.class)
@Import(GlobalExceptionHandler.class)
class ListaEsperaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListaEsperaService listaEsperaService;

    @Test
    void adicionarNaLista_deveRetornar201() throws Exception {
        String body = "{\"pacienteId\":1,\"especialidadeId\":1,\"prioridade\":0}";
        ListaEsperaResponse response = new ListaEsperaResponse();
        response.setId(1);
        response.setPacienteId(1);
        response.setEspecialidadeId(1);
        when(listaEsperaService.adicionarNaLista(any())).thenReturn(response);

        mockMvc.perform(post("/api/lista-espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pacienteId").value(1));
    }

    @Test
    void buscarPorId_deveRetornar200() throws Exception {
        ListaEsperaResponse response = new ListaEsperaResponse();
        response.setId(1);
        when(listaEsperaService.buscarPorId(1)).thenReturn(response);

        mockMvc.perform(get("/api/lista-espera/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void listar_semFiltros_deveRetornar200() throws Exception {
        ListaEsperaResponse response = new ListaEsperaResponse();
        response.setId(1);
        when(listaEsperaService.listarTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/lista-espera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listar_comFiltros_deveRetornar200() throws Exception {
        ListaEsperaResponse response = new ListaEsperaResponse();
        response.setId(1);
        when(listaEsperaService.listarPorFiltros(1, null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/lista-espera").param("especialidadeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void obterProximo_encontrado_deveRetornar200() throws Exception {
        ListaEsperaResponse response = new ListaEsperaResponse();
        response.setId(1);
        when(listaEsperaService.obterProximoDaFila(any(), any(), any())).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/lista-espera/proximo").param("especialidadeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obterProximo_naoEncontrado_deveRetornar404() throws Exception {
        when(listaEsperaService.obterProximoDaFila(any(), any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lista-espera/proximo").param("especialidadeId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerDaLista_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/lista-espera/1"))
                .andExpect(status().isNoContent());
    }
}
