package it.unicam.cs.ids.hackhub.exception;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

	@ExceptionHandler(DuplicateEvaluationException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicateEvaluation(
			DuplicateEvaluationException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(InvalidEvaluationException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidEvaluation(
			InvalidEvaluationException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
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

	/**
	 * Binding di un parametro fallito (es. valore non valido per un enum come
	 * {@code status}). Restituisce 400 con messaggio coerente, elencando i
	 * valori ammessi quando il tipo atteso è un enum.
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleTypeMismatch(
			MethodArgumentTypeMismatchException ex) {
		Class<?> required = ex.getRequiredType();
		String message = "Valore non valido per '" + ex.getName() + "': " + ex.getValue();
		if (required != null && required.isEnum()) {
			message += ". Valori ammessi: " + java.util.Arrays.toString(required.getEnumConstants());
		}
		return build(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
		// Messaggio generico: evita user enumeration.
		return build(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleUserNotFound(UsernameNotFoundException ex) {
		return build(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
		return build(HttpStatus.FORBIDDEN, "Accesso negato: permessi insufficienti");
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
		return build(HttpStatus.UNAUTHORIZED, "Autenticazione fallita");
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
