package com.fiap.vaga_liberada_agenda.repository;

import com.fiap.vaga_liberada_agenda.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Integer> {
    
    Optional<Medico> findByCrm(String crm);
    
    boolean existsByCrm(String crm);
    
    List<Medico> findByEspecialidadeId(Integer especialidadeId);
    
    List<Medico> findByUnidadeId(Integer unidadeId);
    
    List<Medico> findByAtivo(Boolean ativo);
}
