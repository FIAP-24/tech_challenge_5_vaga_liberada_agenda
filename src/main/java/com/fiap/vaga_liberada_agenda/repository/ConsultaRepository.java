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
    
    // Busca consultas pendentes de confirmação que já passaram do prazo
    @Query("SELECT c FROM Consulta c WHERE c.status = :status " +
           "AND c.dataLimiteConfirmacao IS NOT NULL " +
           "AND c.dataLimiteConfirmacao <= :agora " +
           "AND c.confirmadaEm IS NULL")
    List<Consulta> buscarConsultasNaoConfirmadas(
            @Param("status") StatusConsulta status,
            @Param("agora") LocalDateTime agora
    );
    
    // Busca consultas por status
    List<Consulta> findByStatus(StatusConsulta status);
    
    // Busca consultas por paciente
    List<Consulta> findByPacienteId(Integer pacienteId);
    
    // Verifica se já existe consulta agendada no mesmo horário
    @Query("SELECT COUNT(c) > 0 FROM Consulta c WHERE c.medico.id = :medicoId " +
           "AND c.dataHora = :dataHora " +
           "AND c.status IN ('AGENDADA', 'PENDENTE_CONFIRMACAO')")
    boolean existsConsultaNoHorario(
            @Param("medicoId") Integer medicoId,
            @Param("dataHora") LocalDateTime dataHora
    );
    
    // Verifica se há consultas para um médico
    @Query("SELECT COUNT(c) > 0 FROM Consulta c WHERE c.medico.id = :medicoId")
    boolean existsByMedicoId(@Param("medicoId") Integer medicoId);
    
    // Verifica se há consultas para uma unidade
    @Query("SELECT COUNT(c) > 0 FROM Consulta c WHERE c.unidade.id = :unidadeId")
    boolean existsByUnidadeId(@Param("unidadeId") Integer unidadeId);
    
    // Verifica se há consultas para um paciente
    @Query("SELECT COUNT(c) > 0 FROM Consulta c WHERE c.paciente.id = :pacienteId")
    boolean existsByPacienteId(@Param("pacienteId") Integer pacienteId);
}
