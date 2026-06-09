package it.unicam.cs.ids.hackhub.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Risponde 403 JSON per ruolo/ownership insufficiente.
 * Formato coerente con {@code GlobalExceptionHandler}.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException)
			throws IOException {

		RestAuthenticationEntryPoint.ErrorJson.write(
				response,
				HttpStatus.FORBIDDEN,
				"Accesso negato: permessi insufficienti",
				request.getRequestURI());
	}
}
