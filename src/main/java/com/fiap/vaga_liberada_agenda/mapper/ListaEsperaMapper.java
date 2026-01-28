package com.fiap.vaga_liberada_agenda.mapper;

import com.fiap.vaga_liberada_agenda.dto.response.ListaEsperaResponse;
import com.fiap.vaga_liberada_agenda.entity.ListaEspera;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ListaEsperaMapper {

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNome", source = "paciente.nome")
    @Mapping(target = "especialidadeId", source = "especialidade.id")
    @Mapping(target = "especialidadeNome", source = "especialidade.nome")
    @Mapping(target = "medicoId", source = "medico.id", ignore = true)
    @Mapping(target = "medicoNome", source = "medico.nome", ignore = true)
    @Mapping(target = "unidadeId", source = "unidade.id", ignore = true)
    @Mapping(target = "unidadeNome", source = "unidade.nome", ignore = true)
    @Mapping(target = "consultaOferecidaId", source = "consultaOferecida.id", ignore = true)
    ListaEsperaResponse toResponse(ListaEspera listaEspera);

    default ListaEsperaResponse toResponseWithDetails(ListaEspera listaEspera) {
        ListaEsperaResponse response = toResponse(listaEspera);
        if (listaEspera.getMedico() != null) {
            response.setMedicoId(listaEspera.getMedico().getId());
            response.setMedicoNome(listaEspera.getMedico().getNome());
        }
        if (listaEspera.getUnidade() != null) {
            response.setUnidadeId(listaEspera.getUnidade().getId());
            response.setUnidadeNome(listaEspera.getUnidade().getNome());
        }
        if (listaEspera.getConsultaOferecida() != null) {
            response.setConsultaOferecidaId(listaEspera.getConsultaOferecida().getId());
        }
        return response;
    }
}
