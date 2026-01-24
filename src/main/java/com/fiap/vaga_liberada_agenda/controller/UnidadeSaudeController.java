package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.request.UnidadeSaudeRequest;
import com.fiap.vaga_liberada_agenda.dto.response.UnidadeSaudeResponse;
import com.fiap.vaga_liberada_agenda.service.UnidadeSaudeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades-saude")
@RequiredArgsConstructor
public class UnidadeSaudeController {

    private final UnidadeSaudeService unidadeSaudeService;

    @PostMapping
    public ResponseEntity<UnidadeSaudeResponse> criar(@Valid @RequestBody UnidadeSaudeRequest request) {
        UnidadeSaudeResponse response = unidadeSaudeService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeSaudeResponse> buscarPorId(@PathVariable Integer id) {
        UnidadeSaudeResponse response = unidadeSaudeService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UnidadeSaudeResponse>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<UnidadeSaudeResponse> response = unidadeSaudeService.listar(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<UnidadeSaudeResponse>> listarTodos(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String bairro) {
        List<UnidadeSaudeResponse> response = unidadeSaudeService.listarComFiltros(cidade, bairro);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadeSaudeResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UnidadeSaudeRequest request) {
        UnidadeSaudeResponse response = unidadeSaudeService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        unidadeSaudeService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
