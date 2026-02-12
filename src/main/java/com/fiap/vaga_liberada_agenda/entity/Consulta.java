package com.fiap.vaga_liberada_agenda.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "vagaOferecidaParaListaEspera")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id")
    private UnidadeSaude unidade;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status = StatusConsulta.PENDENTE_CONFIRMACAO;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "lembrete_enviado")
    private Boolean lembreteEnviado = false;

    @Column(name = "confirmada_em")
    private LocalDateTime confirmadaEm;

    @Column(name = "data_limite_confirmacao")
    private LocalDateTime dataLimiteConfirmacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_oferecida_para_lista_espera_id")
    private ListaEspera vagaOferecidaParaListaEspera;

    @Column(name = "vaga_oferecida_em")
    private LocalDateTime vagaOferecidaEm;
}
