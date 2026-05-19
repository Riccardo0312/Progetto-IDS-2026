package it.unicam.cs.ids.hackhub.model.state.hackathon;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;

public final class EvaluationState implements HackathonState {

	@Override
	public HackathonStatus getStatus() {
		return HackathonStatus.EVALUATION;
	}

	@Override
	public void ensureJudgingActionsAllowed(Long hackathonId) {
	}

	@Override
	public void ensureWinnerProclamationAllowed(Long hackathonId) {
	}

}
