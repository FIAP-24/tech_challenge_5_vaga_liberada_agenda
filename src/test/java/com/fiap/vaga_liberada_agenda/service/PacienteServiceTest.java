package com.fiap.vaga_liberada_agenda.service;

import com.fiap.vaga_liberada_agenda.dto.request.PacienteRequest;
import com.fiap.vaga_liberada_agenda.dto.response.PacienteResponse;
import com.fiap.vaga_liberada_agenda.entity.Paciente;
import com.fiap.vaga_liberada_agenda.mapper.PacienteMapper;
import com.fiap.vaga_liberada_agenda.repository.ConsultaRepository;
import com.fiap.vaga_liberada_agenda.repository.ListaEsperaRepository;
import com.fiap.vaga_liberada_agenda.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private PacienteMapper pacienteMapper;
    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private ListaEsperaRepository listaEsperaRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteRequest request;
    private PacienteResponse response;

    @BeforeEach
    void setup() {
        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNome("João Silva");
        paciente.setCpf("12345678901");
        paciente.setCartaoSus("123456789012345");

        request = new PacienteRequest();
        request.setNome("João Silva");
        request.setCpf("12345678901");
        request.setCartaoSus("123456789012345");

        response = new PacienteResponse();
        response.setId(1);
        response.setNome("João Silva");
    }

    @Test
    void criar_deveRetornarPacienteCriado() {
        when(pacienteRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(pacienteRepository.existsByCartaoSus(request.getCartaoSus())).thenReturn(false);
        when(pacienteMapper.toEntity(request)).thenReturn(paciente);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PacienteResponse result = pacienteService.criar(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void criar_deveLancarQuandoCpfJaCadastrado() {
        when(pacienteRepository.existsByCpf(request.getCpf())).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF já cadastrado");
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void criar_deveLancarQuandoCartaoSusJaCadastrado() {
        when(pacienteRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(pacienteRepository.existsByCartaoSus(request.getCartaoSus())).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cartão SUS já cadastrado");
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void criar_comCartaoSusNulo_naoVerificaCartaoSus() {
        request.setCartaoSus(null);
        when(pacienteRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(pacienteMapper.toEntity(request)).thenReturn(paciente);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PacienteResponse result = pacienteService.criar(request);

        assertThat(result).isNotNull();
        verify(pacienteRepository, never()).existsByCartaoSus(any());
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void criar_comCartaoSusVazio_naoVerificaCartaoSus() {
        request.setCartaoSus("");
        when(pacienteRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(pacienteMapper.toEntity(request)).thenReturn(paciente);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PacienteResponse result = pacienteService.criar(request);

        assertThat(result).isNotNull();
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void buscarPorId_deveRetornarPaciente() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PacienteResponse result = pacienteService.buscarPorId(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void buscarPorId_deveLancarQuandoNaoEncontrado() {
        when(pacienteRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.buscarPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void buscarPorCpf_deveRetornarPaciente() {
        when(pacienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(paciente));
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PacienteResponse result = pacienteService.buscarPorCpf("12345678901");

        assertThat(result).isNotNull();
    }

    @Test
    void buscarPorCpf_deveLancarQuandoNaoEncontrado() {
        when(pacienteRepository.findByCpf("99999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.buscarPorCpf("99999999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void listar_deveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Paciente> page = new PageImpl<>(List.of(paciente));
        when(pacienteRepository.findAll(pageable)).thenReturn(page);
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        Page<PacienteResponse> result = pacienteService.listar(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void listarTodos_deveRetornarLista() {
        when(pacienteRepository.findAll()).thenReturn(List.of(paciente));
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        List<PacienteResponse> result = pacienteService.listarTodos();

        assertThat(result).hasSize(1);
    }

    @Test
    void atualizar_deveRetornarPacienteAtualizado() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        lenient().when(pacienteRepository.existsByCpf(any())).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PacienteResponse result = pacienteService.atualizar(1, request);

        assertThat(result).isNotNull();
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void atualizar_deveLancarQuandoNaoEncontrado() {
        when(pacienteRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.atualizar(999, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Paciente não encontrado");
    }

    @Test
    void atualizar_deveLancarQuandoCpfAlteradoEDuplicado() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.existsByCpf("99999999999")).thenReturn(true);
        request.setCpf("99999999999");

        assertThatThrownBy(() -> pacienteService.atualizar(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF já cadastrado");
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void atualizar_deveLancarQuandoCartaoSusAlteradoEDuplicado() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        lenient().when(pacienteRepository.existsByCpf(any())).thenReturn(false);
        when(pacienteRepository.existsByCartaoSus("999999999999999")).thenReturn(true);
        request.setCartaoSus("999999999999999");

        assertThatThrownBy(() -> pacienteService.atualizar(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cartão SUS já cadastrado");
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void atualizar_comCartaoSusNulo_naoVerificaCartaoSusDuplicado() {
        request.setCartaoSus(null);
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        lenient().when(pacienteRepository.existsByCpf(any())).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);
        when(pacienteMapper.toResponse(paciente)).thenReturn(response);

        PacienteResponse result = pacienteService.atualizar(1, request);

        assertThat(result).isNotNull();
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void deletar_deveRemoverPaciente() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(consultaRepository.existsByPacienteId(1)).thenReturn(false);
        when(listaEsperaRepository.existsPacienteAtivoNaLista(1)).thenReturn(false);

        pacienteService.deletar(1);

        verify(pacienteRepository).deleteById(1);
    }

    @Test
    void deletar_deveLancarQuandoTemConsultas() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(consultaRepository.existsByPacienteId(1)).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.deletar(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("consultas associadas");
        verify(pacienteRepository, never()).deleteById(any());
    }

    @Test
    void deletar_deveLancarQuandoNaListaEspera() {
        when(pacienteRepository.findById(1)).thenReturn(Optional.of(paciente));
        when(consultaRepository.existsByPacienteId(1)).thenReturn(false);
        when(listaEsperaRepository.existsPacienteAtivoNaLista(1)).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.deletar(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lista de espera");
        verify(pacienteRepository, never()).deleteById(any());
    }
}
