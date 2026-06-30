package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.evaluation.EvaluationResponseDTO;
import it.unicam.cs.ids.hackhub.model.Evaluation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct {@code Evaluation} -> {@link EvaluationResponseDTO}.
 */
@Mapper(componentModel = "spring")
public interface EvaluationMapper {

	@Mapping(target = "submissionId", source = "submission.id")
	@Mapping(target = "judgeId", source = "judge.id")
	EvaluationResponseDTO toResponse(Evaluation evaluation);
}
