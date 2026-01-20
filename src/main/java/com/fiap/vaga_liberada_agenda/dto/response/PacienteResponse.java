package com.fiap.vaga_liberada_agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponse {

    private Integer id;
    private String nome;
    private String cpf;
    private String cartaoSus;
    private String email;
    private String telefone;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String cep;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime criadoEm;
}
