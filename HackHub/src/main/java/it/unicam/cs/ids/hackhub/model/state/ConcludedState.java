package it.unicam.cs.ids.hackhub.model.state;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;

public final class ConcludedState implements HackathonState {

	@Override
	public HackathonStatus getStatus() {
		return HackathonStatus.CONCLUDED;
	}

}
