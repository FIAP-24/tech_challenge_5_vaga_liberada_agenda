package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.response.MedicoResponse;
import com.fiap.vaga_liberada_agenda.exception.GlobalExceptionHandler;
import com.fiap.vaga_liberada_agenda.service.MedicoService;
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

@WebMvcTest(controllers = MedicoController.class)
@Import(GlobalExceptionHandler.class)
class MedicoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicoService medicoService;

    @Test
    void criar_deveRetornar201() throws Exception {
        String body = "{\"nome\":\"Dr. João\",\"crm\":\"12345\",\"especialidadeId\":1,\"unidadeId\":1,\"ativo\":true}";
        MedicoResponse response = new MedicoResponse();
        response.setId(1);
        response.setNome("Dr. João");
        when(medicoService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/api/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Dr. João"));
    }

    @Test
    void buscarPorId_deveRetornar200() throws Exception {
        MedicoResponse response = new MedicoResponse();
        response.setId(1);
        response.setNome("Dr. João");
        when(medicoService.buscarPorId(1)).thenReturn(response);

        mockMvc.perform(get("/api/medicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void buscarPorCrm_deveRetornar200() throws Exception {
        MedicoResponse response = new MedicoResponse();
        response.setId(1);
        response.setCrm("12345");
        when(medicoService.buscarPorCrm("12345")).thenReturn(response);

        mockMvc.perform(get("/api/medicos/crm/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.crm").value("12345"));
    }

    @Test
    void listar_deveRetornar200() throws Exception {
        MedicoResponse response = new MedicoResponse();
        response.setId(1);
        when(medicoService.listarComFiltros(any(), eq(null), eq(null), eq(null)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void atualizar_deveRetornar200() throws Exception {
        String body = "{\"nome\":\"Dr. João Atualizado\",\"crm\":\"12345\",\"especialidadeId\":1,\"unidadeId\":1}";
        MedicoResponse response = new MedicoResponse();
        response.setId(1);
        response.setNome("Dr. João Atualizado");
        when(medicoService.atualizar(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/medicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Dr. João Atualizado"));
    }

    @Test
    void desativar_deveRetornar204() throws Exception {
        mockMvc.perform(patch("/api/medicos/1/desativar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void ativar_deveRetornar204() throws Exception {
        mockMvc.perform(patch("/api/medicos/1/ativar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletar_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/medicos/1"))
                .andExpect(status().isNoContent());
    }
}
