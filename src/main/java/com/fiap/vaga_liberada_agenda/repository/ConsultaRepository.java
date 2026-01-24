package com.fiap.vaga_liberada_agenda.repository;

import com.fiap.vaga_liberada_agenda.entity.Consulta;
import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {
    // Procura consultas agendadas, dentro do horário X e Y, que ainda não tiveram lembrete enviado
    @Query("SELECT c FROM Consulta c WHERE c.dataHora BETWEEN :inicio AND :fim AND c.status = :status AND (c.lembreteEnviado IS NULL OR c.lembreteEnviado = false)")
    List<Consulta> buscarConsultasParaNotificar(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("status") StatusConsulta status
    );
}
