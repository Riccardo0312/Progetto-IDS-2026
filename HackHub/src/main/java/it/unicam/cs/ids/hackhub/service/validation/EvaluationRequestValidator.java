package it.unicam.cs.ids.hackhub.service.validation;

public interface EvaluationRequestValidator {

	EvaluationRequestValidator setNext(EvaluationRequestValidator nextValidator);

	void validate(EvaluationRequestContext context);

}
