package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.support.MentoringCallProposalDTO;
import it.unicam.cs.ids.hackhub.dto.support.SupportRequestResponseDTO;
import it.unicam.cs.ids.hackhub.dto.support.SupportResponseDTO;
import it.unicam.cs.ids.hackhub.model.MentoringCallProposal;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import it.unicam.cs.ids.hackhub.model.SupportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupportRequestMapper {

	@Mapping(target = "hackathonId", source = "hackathon.id")
	@Mapping(target = "teamId", source = "team.id")
	@Mapping(target = "description", source = "message")
	@Mapping(target = "response", source = "supportResponse")
	SupportRequestResponseDTO toResponse(SupportRequest supportRequest);

	@Mapping(target = "supportRequestId", source = "supportRequest.id")
	@Mapping(target = "mentorId", source = "mentor.id")
	SupportResponseDTO toResponse(SupportResponse supportResponse);

	@Mapping(target = "supportRequestId", source = "supportRequest.id")
	@Mapping(target = "mentorId", source = "mentor.id")
	MentoringCallProposalDTO toResponse(MentoringCallProposal callProposal);
}
