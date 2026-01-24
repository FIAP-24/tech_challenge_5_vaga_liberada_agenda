package com.fiap.vaga_liberada_agenda.mapper;

import com.fiap.vaga_liberada_agenda.dto.request.MedicoRequest;
import com.fiap.vaga_liberada_agenda.dto.response.MedicoResponse;
import com.fiap.vaga_liberada_agenda.entity.Medico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicoMapper {

    @Mapping(target = "especialidadeId", source = "especialidade.id")
    @Mapping(target = "especialidadeNome", source = "especialidade.nome")
    @Mapping(target = "unidadeId", source = "unidade.id")
    @Mapping(target = "unidadeNome", source = "unidade.nome")
    MedicoResponse toResponse(Medico medico);

    @Mapping(target = "especialidade", ignore = true)
    @Mapping(target = "unidade", ignore = true)
    Medico toEntity(MedicoRequest request);

    @Mapping(target = "especialidade", ignore = true)
    @Mapping(target = "unidade", ignore = true)
    void updateEntityFromRequest(MedicoRequest request, @MappingTarget Medico medico);
}
