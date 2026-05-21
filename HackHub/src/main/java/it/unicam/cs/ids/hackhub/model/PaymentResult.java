package it.unicam.cs.ids.hackhub.model;

/**
 * Esito restituito dal Sistema di Pagamento esterno tramite {@code IPaymentGateway}.
 *
 * <p>Per questo progetto universitario, il gateway è una simulazione: lo stub di
 * sviluppo restituisce sempre un esito {@code success} con un
 * {@code transactionReference} fittizio.
 */
public record PaymentResult(boolean success, String transactionReference, String failureReason) {

	public static PaymentResult success(String transactionReference) {
		return new PaymentResult(true, transactionReference, null);
	}

	public static PaymentResult failure(String failureReason) {
		return new PaymentResult(false, null, failureReason);
	}
}
