package com.fiap.vaga_liberada_agenda.repository;

import com.fiap.vaga_liberada_agenda.entity.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Integer> {
}
