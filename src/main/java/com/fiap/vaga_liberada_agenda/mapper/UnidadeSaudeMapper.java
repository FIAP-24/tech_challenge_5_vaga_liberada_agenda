package com.fiap.vaga_liberada_agenda.mapper;

import com.fiap.vaga_liberada_agenda.dto.request.UnidadeSaudeRequest;
import com.fiap.vaga_liberada_agenda.dto.response.UnidadeSaudeResponse;
import com.fiap.vaga_liberada_agenda.entity.UnidadeSaude;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UnidadeSaudeMapper {

    UnidadeSaudeResponse toResponse(UnidadeSaude unidadeSaude);

    UnidadeSaude toEntity(UnidadeSaudeRequest request);

    void updateEntityFromRequest(UnidadeSaudeRequest request, @MappingTarget UnidadeSaude unidadeSaude);
}
