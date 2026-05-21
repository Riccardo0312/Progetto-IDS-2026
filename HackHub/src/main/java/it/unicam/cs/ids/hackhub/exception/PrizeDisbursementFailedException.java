package it.unicam.cs.ids.hackhub.exception;

/**
 * Lanciata quando il Sistema di Pagamento esterno restituisce un esito
 * negativo. In HTTP corrisponde a 422 Unprocessable Entity: la richiesta è ben
 * formata, ma il servizio esterno non ha completato l'operazione. Un nuovo
 * tentativo è ammesso.
 */
public class PrizeDisbursementFailedException extends RuntimeException {

	public PrizeDisbursementFailedException(Long hackathonId, String reason) {
		super("Erogazione del premio fallita per l'hackathon " + hackathonId + ": " + reason);
	}
}
