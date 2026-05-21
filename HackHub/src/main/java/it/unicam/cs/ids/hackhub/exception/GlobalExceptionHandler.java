package it.unicam.cs.ids.hackhub.exception;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Mappa le eccezioni di dominio su risposte HTTP coerenti per le API REST.
 *
 * <p>Convenzioni adottate:
 * <ul>
 * <li>404 Not Found per risorse mancanti.</li>
 * <li>403 Forbidden per azioni non autorizzate per l'attore corrente.</li>
 * <li>409 Conflict per stati incompatibili e per atti già conclusi (es. premio
 * già erogato).</li>
 * <li>422 Unprocessable Entity per esiti negativi del Sistema di Pagamento
 * esterno.</li>
 * <li>400 Bad Request per input invalido.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(ForbiddenOperationException.class)
	public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenOperationException ex) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(InvalidHackathonStateException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidState(
			InvalidHackathonStateException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(PrizeAlreadyDisbursedException.class)
	public ResponseEntity<Map<String, Object>> handleAlreadyDisbursed(
			PrizeAlreadyDisbursedException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(PrizeDisbursementFailedException.class)
	public ResponseEntity<Map<String, Object>> handleDisbursementFailed(
			PrizeDisbursementFailedException ex) {
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
		Map<String, Object> body = Map.of(
				"timestamp", LocalDateTime.now(),
				"status", status.value(),
				"error", status.getReasonPhrase(),
				"message", message == null ? "" : message);
		return ResponseEntity.status(status).body(body);
	}
}
