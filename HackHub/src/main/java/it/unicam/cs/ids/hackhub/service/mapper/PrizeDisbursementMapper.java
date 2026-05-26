package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.model.PrizeDisbursement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrizeDisbursementMapper {

	@Mapping(target = "hackathonId", source = "hackathon.id")
	@Mapping(target = "winningTeamId", source = "winningTeam.id")
	PrizeDisbursementResponseDTO toResponse(PrizeDisbursement disbursement);
}
