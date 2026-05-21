package it.unicam.cs.ids.hackhub.exception;

/**
 * Lanciata quando l'organizzatore tenta di erogare il premio per un hackathon
 * il cui registro {@code PrizeDisbursement} ha già esito {@code SUCCESS}.
 * L'erogazione è un atto unico: in HTTP corrisponde a 409 Conflict.
 */
public class PrizeAlreadyDisbursedException extends RuntimeException {

	public PrizeAlreadyDisbursedException(Long hackathonId) {
		super("Il premio dell'hackathon " + hackathonId + " è già stato erogato");
	}
}
