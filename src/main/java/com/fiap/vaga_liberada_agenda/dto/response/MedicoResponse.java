package com.fiap.vaga_liberada_agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoResponse {

    private Integer id;
    private String nome;
    private String crm;
    private Integer especialidadeId;
    private String especialidadeNome;
    private Integer unidadeId;
    private String unidadeNome;
    private Boolean ativo;
}
