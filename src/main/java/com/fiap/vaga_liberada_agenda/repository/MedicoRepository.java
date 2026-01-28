package com.fiap.vaga_liberada_agenda.repository;

import com.fiap.vaga_liberada_agenda.entity.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Integer> {

    Optional<Medico> findByCrm(String crm);

    boolean existsByCrm(String crm);
    
    boolean existsByUnidadeId(Integer unidadeId);

    // Métodos para buscar com paginação e filtros
    Page<Medico> findByEspecialidadeIdAndUnidadeIdAndAtivo(
            Integer especialidadeId,
            Integer unidadeId,
            Boolean ativo,
            org.springframework.data.domain.Pageable pageable);

    Page<Medico> findByEspecialidadeIdAndUnidadeId(
            Integer especialidadeId,
            Integer unidadeId,
            Pageable pageable);

    Page<Medico> findByEspecialidadeIdAndAtivo(
            Integer especialidadeId,
            Boolean ativo,
            org.springframework.data.domain.Pageable pageable);

    Page<Medico> findByUnidadeIdAndAtivo(
            Integer unidadeId,
            Boolean ativo,
            org.springframework.data.domain.Pageable pageable);

    Page<Medico> findByEspecialidadeId(
            Integer especialidadeId,
            org.springframework.data.domain.Pageable pageable);

    Page<Medico> findByUnidadeId(
            Integer unidadeId,
            org.springframework.data.domain.Pageable pageable);

    Page<Medico> findByAtivo(
            Boolean ativo,
            org.springframework.data.domain.Pageable pageable);
}
