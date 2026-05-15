package it.unicam.cs.ids.hackhub.exception;

public class DuplicateEvaluationException extends RuntimeException {

	public DuplicateEvaluationException(Long submissionId) {
		super("Submission " + submissionId + " has already been evaluated");
	}

}
