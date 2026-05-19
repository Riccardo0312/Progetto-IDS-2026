package it.unicam.cs.ids.hackhub.service.validation;

public abstract class AbstractEvaluationRequestValidator implements EvaluationRequestValidator {

	private EvaluationRequestValidator nextValidator;

	@Override
	public EvaluationRequestValidator setNext(EvaluationRequestValidator nextValidator) {
		this.nextValidator = nextValidator;
		return nextValidator;
	}

	@Override
	public final void validate(EvaluationRequestContext context) {
		validateCurrent(context);

		if (nextValidator != null) {
			nextValidator.validate(context);
		}
	}

	protected abstract void validateCurrent(EvaluationRequestContext context);

}
