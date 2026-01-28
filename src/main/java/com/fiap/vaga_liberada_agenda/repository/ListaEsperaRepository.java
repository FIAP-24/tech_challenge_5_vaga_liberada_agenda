package com.fiap.vaga_liberada_agenda.repository;

import com.fiap.vaga_liberada_agenda.entity.ListaEspera;
import com.fiap.vaga_liberada_agenda.entity.StatusListaEspera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Integer> {
    
    List<ListaEspera> findByStatus(StatusListaEspera status);
    
    List<ListaEspera> findByEspecialidadeIdAndStatus(Integer especialidadeId, StatusListaEspera status);
    
    List<ListaEspera> findByMedicoIdAndStatus(Integer medicoId, StatusListaEspera status);
    
    List<ListaEspera> findByUnidadeIdAndStatus(Integer unidadeId, StatusListaEspera status);
    
    List<ListaEspera> findByPacienteIdAndStatus(Integer pacienteId, StatusListaEspera status);
    
    Optional<ListaEspera> findFirstByStatusOrderByDataCadastroAsc(StatusListaEspera status);
    
    @Query("SELECT le FROM ListaEspera le WHERE le.status = :status " +
           "AND (:especialidadeId IS NULL OR le.especialidade.id = :especialidadeId) " +
           "AND (:medicoId IS NULL OR le.medico.id = :medicoId) " +
           "AND (:unidadeId IS NULL OR le.unidade.id = :unidadeId) " +
           "ORDER BY le.prioridade DESC, le.dataCadastro ASC")
    List<ListaEspera> findByFiltros(
            @Param("status") StatusListaEspera status,
            @Param("especialidadeId") Integer especialidadeId,
            @Param("medicoId") Integer medicoId,
            @Param("unidadeId") Integer unidadeId
    );
    
    @Query("SELECT le FROM ListaEspera le WHERE le.status = :status " +
           "AND (:especialidadeId IS NULL OR le.especialidade.id = :especialidadeId) " +
           "AND (:medicoId IS NULL OR le.medico.id = :medicoId) " +
           "AND (:unidadeId IS NULL OR le.unidade.id = :unidadeId) " +
           "ORDER BY le.prioridade DESC, le.dataCadastro ASC")
    Optional<ListaEspera> findFirstByFiltros(
            @Param("status") StatusListaEspera status,
            @Param("especialidadeId") Integer especialidadeId,
            @Param("medicoId") Integer medicoId,
            @Param("unidadeId") Integer unidadeId
    );
    
    // Verifica se há registros ativos na lista de espera para um paciente
    @Query("SELECT COUNT(le) > 0 FROM ListaEspera le WHERE le.paciente.id = :pacienteId " +
           "AND le.status IN ('ATIVA', 'AGUARDANDO_RESPOSTA')")
    boolean existsPacienteAtivoNaLista(@Param("pacienteId") Integer pacienteId);
}
