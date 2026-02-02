package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.response.UnidadeSaudeResponse;
import com.fiap.vaga_liberada_agenda.exception.GlobalExceptionHandler;
import com.fiap.vaga_liberada_agenda.service.UnidadeSaudeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UnidadeSaudeController.class)
@Import(GlobalExceptionHandler.class)
class UnidadeSaudeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnidadeSaudeService unidadeSaudeService;

    @Test
    void criar_deveRetornar201() throws Exception {
        String body = "{\"nome\":\"UBS Centro\",\"cidade\":\"São Paulo\",\"latitude\":-23.55,\"longitude\":-46.63}";
        UnidadeSaudeResponse response = new UnidadeSaudeResponse();
        response.setId(1);
        response.setNome("UBS Centro");
        when(unidadeSaudeService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/api/unidades-saude")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("UBS Centro"));
    }

    @Test
    void buscarPorId_deveRetornar200() throws Exception {
        UnidadeSaudeResponse response = new UnidadeSaudeResponse();
        response.setId(1);
        response.setNome("UBS Centro");
        when(unidadeSaudeService.buscarPorId(1)).thenReturn(response);

        mockMvc.perform(get("/api/unidades-saude/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void listar_semFiltros_deveRetornar200() throws Exception {
        UnidadeSaudeResponse response = new UnidadeSaudeResponse();
        response.setId(1);
        when(unidadeSaudeService.listar(any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/unidades-saude"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void listar_comCidade_deveRetornar200() throws Exception {
        UnidadeSaudeResponse response = new UnidadeSaudeResponse();
        response.setId(1);
        when(unidadeSaudeService.listarComFiltros(eq("São Paulo"), eq(null))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/unidades-saude").param("cidade", "São Paulo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void atualizar_deveRetornar200() throws Exception {
        String body = "{\"nome\":\"UBS Atualizada\",\"cidade\":\"São Paulo\",\"latitude\":-23.55,\"longitude\":-46.63}";
        UnidadeSaudeResponse response = new UnidadeSaudeResponse();
        response.setId(1);
        response.setNome("UBS Atualizada");
        when(unidadeSaudeService.atualizar(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/unidades-saude/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("UBS Atualizada"));
    }

    @Test
    void deletar_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/unidades-saude/1"))
                .andExpect(status().isNoContent());
    }
}
