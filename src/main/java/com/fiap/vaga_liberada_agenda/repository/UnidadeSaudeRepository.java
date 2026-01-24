package com.fiap.vaga_liberada_agenda.repository;

import com.fiap.vaga_liberada_agenda.entity.UnidadeSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadeSaudeRepository extends JpaRepository<UnidadeSaude, Integer> {
    
    List<UnidadeSaude> findByCidade(String cidade);
    
    List<UnidadeSaude> findByBairro(String bairro);
    
    List<UnidadeSaude> findByCidadeAndBairro(String cidade, String bairro);
    
    boolean existsByNome(String nome);
}
