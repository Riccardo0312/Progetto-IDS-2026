package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.submission.SubmissionResponseDTO;
import it.unicam.cs.ids.hackhub.model.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct {@code Submission} -> {@link SubmissionResponseDTO}.
 */
@Mapper(componentModel = "spring")
public interface SubmissionMapper {

	@Mapping(target = "registrationId", source = "registration.id")
	SubmissionResponseDTO toResponse(Submission submission);
}
