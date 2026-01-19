package com.fiap.vaga_liberada_agenda.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    private Consulta consulta;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoNotificacao tipo = TipoNotificacao.EMAIL;

    @Column(name = "enviada_em", updatable = false)
    private LocalDateTime enviadaEm;

    @Column(nullable = false)
    private Boolean lida = false;

    @PrePersist
    protected void onCreate() {
        enviadaEm = LocalDateTime.now();
    }
}
