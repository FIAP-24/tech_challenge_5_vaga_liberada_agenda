package com.fiap.vaga_liberada_agenda.mapper;

import com.fiap.vaga_liberada_agenda.dto.response.ConsultaResponse;
import com.fiap.vaga_liberada_agenda.entity.Consulta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConsultaMapper {

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNome", source = "paciente.nome")
    @Mapping(target = "medicoId", source = "medico.id")
    @Mapping(target = "medicoNome", source = "medico.nome")
    @Mapping(target = "medicoCrm", source = "medico.crm")
    @Mapping(target = "unidadeId", source = "unidade.id")
    @Mapping(target = "unidadeNome", source = "unidade.nome")
    ConsultaResponse toResponse(Consulta consulta);
}
