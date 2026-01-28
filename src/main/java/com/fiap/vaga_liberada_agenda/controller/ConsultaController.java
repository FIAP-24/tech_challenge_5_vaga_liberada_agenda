package com.fiap.vaga_liberada_agenda.controller;

import com.fiap.vaga_liberada_agenda.dto.request.ConsultaRequest;
import com.fiap.vaga_liberada_agenda.dto.response.ConsultaResponse;
import com.fiap.vaga_liberada_agenda.entity.StatusConsulta;
import com.fiap.vaga_liberada_agenda.service.ConsultaService;
import com.fiap.vaga_liberada_agenda.service.LiberacaoVagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;
    private final LiberacaoVagaService liberacaoVagaService;

    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(@Valid @RequestBody ConsultaRequest request) {
        ConsultaResponse response = consultaService.agendar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> buscarPorId(@PathVariable Integer id) {
        ConsultaResponse response = consultaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listar(
            @RequestParam(required = false) Integer pacienteId,
            @RequestParam(required = false) String status) {
        
        if (pacienteId != null) {
            List<ConsultaResponse> response = consultaService.listarPorPaciente(pacienteId);
            return ResponseEntity.ok(response);
        }
        
        if (status != null) {
            try {
                StatusConsulta statusConsulta = StatusConsulta.valueOf(status.toUpperCase());
                List<ConsultaResponse> response = consultaService.listarPorStatus(statusConsulta);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        List<ConsultaResponse> response = consultaService.listarTodas();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ConsultaResponse> confirmarConsulta(@PathVariable Integer id) {
        ConsultaResponse response = consultaService.confirmarConsulta(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaResponse> cancelarConsulta(@PathVariable Integer id) {
        ConsultaResponse response = consultaService.cancelarConsulta(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desistir")
    public ResponseEntity<ConsultaResponse> desistirConsulta(@PathVariable Integer id) {
        ConsultaResponse response = consultaService.desistirConsulta(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/aceitar-vaga")
    public ResponseEntity<Void> aceitarVaga(
            @PathVariable Integer id,
            @RequestParam Integer listaEsperaId) {
        liberacaoVagaService.aceitarVaga(id, listaEsperaId);
        return ResponseEntity.ok().build();
    }
}
