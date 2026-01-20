package com.fiap.vaga_liberada_agenda.repository;

import com.fiap.vaga_liberada_agenda.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    
    Optional<Paciente> findByCpf(String cpf);
    
    Optional<Paciente> findByCartaoSus(String cartaoSus);
    
    boolean existsByCpf(String cpf);
    
    boolean existsByCartaoSus(String cartaoSus);
}
