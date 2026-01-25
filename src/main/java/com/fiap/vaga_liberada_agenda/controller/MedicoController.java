package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.request.MedicoRequest;
import com.fiap.vaga_liberada_agenda.dto.response.MedicoResponse;
import com.fiap.vaga_liberada_agenda.service.MedicoService;
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
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @PostMapping
    public ResponseEntity<MedicoResponse> criar(@Valid @RequestBody MedicoRequest request) {
        MedicoResponse response = medicoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> buscarPorId(@PathVariable Integer id) {
        MedicoResponse response = medicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/crm/{crm}")
    public ResponseEntity<MedicoResponse> buscarPorCrm(@PathVariable String crm) {
        MedicoResponse response = medicoService.buscarPorCrm(crm);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<MedicoResponse>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            @RequestParam(required = false) Integer especialidadeId,
            @RequestParam(required = false) Integer unidadeId,
            @RequestParam(required = false) Boolean ativo) {
        Page<MedicoResponse> response = medicoService.listarComFiltros(pageable, especialidadeId, unidadeId, ativo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody MedicoRequest request) {
        MedicoResponse response = medicoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Integer id) {
        medicoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Integer id) {
        medicoService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        medicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
