package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.request.ListaEsperaRequest;
import com.fiap.vaga_liberada_agenda.dto.response.ListaEsperaResponse;
import com.fiap.vaga_liberada_agenda.service.ListaEsperaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lista-espera")
@RequiredArgsConstructor
public class ListaEsperaController {

    private final ListaEsperaService listaEsperaService;

    @PostMapping
    public ResponseEntity<ListaEsperaResponse> adicionarNaLista(@Valid @RequestBody ListaEsperaRequest request) {
        ListaEsperaResponse response = listaEsperaService.adicionarNaLista(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaEsperaResponse> buscarPorId(@PathVariable Integer id) {
        ListaEsperaResponse response = listaEsperaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ListaEsperaResponse>> listar(
            @RequestParam(required = false) Integer especialidadeId,
            @RequestParam(required = false) Integer medicoId,
            @RequestParam(required = false) Integer unidadeId) {
        // Se não houver filtros, retorna todos; caso contrário, retorna filtrado
        if (especialidadeId == null && medicoId == null && unidadeId == null) {
            List<ListaEsperaResponse> response = listaEsperaService.listarTodos();
            return ResponseEntity.ok(response);
        }
        List<ListaEsperaResponse> response = listaEsperaService.listarPorFiltros(especialidadeId, medicoId, unidadeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/proximo")
    public ResponseEntity<ListaEsperaResponse> obterProximo(
            @RequestParam(required = false) Integer especialidadeId,
            @RequestParam(required = false) Integer medicoId,
            @RequestParam(required = false) Integer unidadeId) {
        Optional<ListaEsperaResponse> response = listaEsperaService.obterProximoDaFila(especialidadeId, medicoId, unidadeId);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerDaLista(@PathVariable Integer id) {
        listaEsperaService.removerDaLista(id);
        return ResponseEntity.noContent().build();
    }
}
