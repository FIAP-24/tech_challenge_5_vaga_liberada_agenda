package com.fiap.vaga_liberada_agenda.mapper;

import com.fiap.vaga_liberada_agenda.dto.request.PacienteRequest;
import com.fiap.vaga_liberada_agenda.dto.response.PacienteResponse;
import com.fiap.vaga_liberada_agenda.entity.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PacienteMapper {

    PacienteResponse toResponse(Paciente paciente);

    Paciente toEntity(PacienteRequest request);

    void updateEntityFromRequest(PacienteRequest request, @MappingTarget Paciente paciente);
}
