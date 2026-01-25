package com.fiap.vaga_liberada_agenda.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaRequest {
    
    @NotNull(message = "ID do paciente é obrigatório")
    private Integer pacienteId;
    
    @NotNull(message = "ID do médico é obrigatório")
    private Integer medicoId;
    
    @NotNull(message = "ID da unidade de saúde é obrigatório")
    private Integer unidadeId;
    
    @NotNull(message = "Data e hora da consulta são obrigatórias")
    @Future(message = "A data da consulta deve ser futura")
    private LocalDateTime dataHora;
    
    private String observacoes;
}
