package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.support.ViolationReportDTO;
import it.unicam.cs.ids.hackhub.model.ViolationReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ViolationReportMapper {

    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "mentorName", source = "mentor.name")
    ViolationReportDTO toDto(ViolationReport violationReport);
}
