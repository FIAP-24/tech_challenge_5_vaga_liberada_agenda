package com.fiap.vaga_liberada_agenda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;

    @NotBlank(message = "CRM é obrigatório")
    @Size(max = 20, message = "CRM deve ter no máximo 20 caracteres")
    private String crm;

    @NotNull(message = "Especialidade é obrigatória")
    private Integer especialidadeId;

    @NotNull(message = "Unidade de Saúde é obrigatória")
    private Integer unidadeId;

    private Boolean ativo = true;
}
