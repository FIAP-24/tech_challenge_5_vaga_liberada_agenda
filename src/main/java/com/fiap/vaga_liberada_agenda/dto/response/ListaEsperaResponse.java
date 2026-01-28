package com.fiap.vaga_liberada_agenda.dto.response;

import com.fiap.vaga_liberada_agenda.entity.StatusListaEspera;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListaEsperaResponse {
    private Integer id;
    private Integer pacienteId;
    private String pacienteNome;
    private Integer especialidadeId;
    private String especialidadeNome;
    private Integer medicoId;
    private String medicoNome;
    private Integer unidadeId;
    private String unidadeNome;
    private LocalDateTime dataCadastro;
    private Integer prioridade;
    private StatusListaEspera status;
    private Integer consultaOferecidaId;
    private LocalDateTime dataOferta;
}
