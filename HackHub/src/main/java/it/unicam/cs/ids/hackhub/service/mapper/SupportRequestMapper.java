package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.support.SupportRequestResponseDTO;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupportRequestMapper {

	@Mapping(target = "hackathonId", source = "hackathon.id")
	@Mapping(target = "teamId", source = "team.id")
	@Mapping(target = "description", source = "message")
	SupportRequestResponseDTO toResponse(SupportRequest supportRequest);
}
