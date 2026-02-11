package com.fiap.vaga_liberada_agenda.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgumentException_deveRetornar400() {
        IllegalArgumentException ex = new IllegalArgumentException("Mensagem de erro");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("mensagem")).isEqualTo("Mensagem de erro");
        assertThat(response.getBody().get("status")).isEqualTo("400");
    }

    @Test
    void handleNoResourceFoundException_deveRetornar404() {
        NoResourceFoundException ex = new NoResourceFoundException(null, "api/health");

        ResponseEntity<Map<String, String>> response = handler.handleNoResourceFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("mensagem")).contains("api/health");
        assertThat(response.getBody().get("status")).isEqualTo("404");
    }

    @Test
    void handleGenericException_deveRetornar500() {
        Exception ex = new RuntimeException("Erro interno");

        ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("mensagem")).isEqualTo("Erro interno do servidor");
        assertThat(response.getBody().get("status")).isEqualTo("500");
    }

    @Test
    void handleValidationExceptions_deveRetornar400ComErrosDeCampo() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.List.of());

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("mensagem")).isEqualTo("Erro de validação nos campos");
    }
}
