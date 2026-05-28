package it.unicam.cs.ids.hackhub.model.state.hackathon;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;

/**
 * Stato terminale: hackathon annullato dall'organizzatore prima dell'inizio.
 * Tutte le azioni sono rifiutate (eredita i default-deny). Vedi ADR 0001.
 */
public final class CancelledState implements HackathonState {

	@Override
	public HackathonStatus getStatus() {
		return HackathonStatus.CANCELLED;
	}

}
