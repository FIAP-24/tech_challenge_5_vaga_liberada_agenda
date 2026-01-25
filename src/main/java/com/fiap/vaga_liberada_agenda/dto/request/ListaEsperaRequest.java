package com.fiap.vaga_liberada_agenda.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListaEsperaRequest {
    
    @NotNull(message = "ID do paciente é obrigatório")
    private Integer pacienteId;
    
    @NotNull(message = "ID da especialidade é obrigatório")
    private Integer especialidadeId;
    
    private Integer medicoId;
    
    private Integer unidadeId;
    
    private Integer prioridade = 0;
}
