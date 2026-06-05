package it.unicam.cs.ids.hackhub.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Risponde 401 JSON per richieste non autenticate / token invalido.
 * Formato coerente con {@code GlobalExceptionHandler}.
 *
 * <p>JSON costruito a mano per non dipendere da uno specifico bean Jackson
 * (Spring Boot 4 usa Jackson 3, package {@code tools.jackson}).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException)
			throws IOException {

		ErrorJson.write(
				response,
				HttpStatus.UNAUTHORIZED,
				"Autenticazione richiesta o token non valido",
				request.getRequestURI());
	}

	/** Helper condiviso per scrivere il body di errore JSON. */
	static final class ErrorJson {
		private ErrorJson() {}

		static void write(HttpServletResponse response, HttpStatus status, String message, String path)
				throws IOException {
			response.setStatus(status.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			String body = "{"
					+ "\"timestamp\":\"" + LocalDateTime.now() + "\","
					+ "\"status\":" + status.value() + ","
					+ "\"error\":\"" + esc(status.getReasonPhrase()) + "\","
					+ "\"message\":\"" + esc(message) + "\","
					+ "\"path\":\"" + esc(path) + "\"}";
			response.getWriter().write(body);
		}

		private static String esc(String s) {
			if (s == null) {
				return "";
			}
			return s.replace("\\", "\\\\").replace("\"", "\\\"");
		}
	}
}
