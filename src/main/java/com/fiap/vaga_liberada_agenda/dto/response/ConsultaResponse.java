package com.fiap.vaga_liberada_agenda.dto.response;

import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaResponse {
    private Integer id;
    private Integer pacienteId;
    private String pacienteNome;
    private Integer medicoId;
    private String medicoNome;
    private String medicoCrm;
    private Integer unidadeId;
    private String unidadeNome;
    private LocalDateTime dataHora;
    private StatusConsulta status;
    private String observacoes;
    private Boolean lembreteEnviado;
    private LocalDateTime confirmadaEm;
    private LocalDateTime dataLimiteConfirmacao;
    private LocalDateTime vagaOferecidaEm;
}
