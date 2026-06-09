package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.auth.AuthenticationResponse;
import it.unicam.cs.ids.hackhub.dto.auth.CurrentUserResponse;
import it.unicam.cs.ids.hackhub.dto.auth.LoginRequest;
import it.unicam.cs.ids.hackhub.dto.auth.RegisterRequest;
import it.unicam.cs.ids.hackhub.security.AuthenticationService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint di autenticazione.
 *
 * <ul>
 * <li>{@code POST /api/auth/register} e {@code POST /api/auth/login}: pubblici.</li>
 * <li>{@code GET /api/auth/me} e {@code POST /api/auth/logout}: autenticati.</li>
 * </ul>
 *
 * <p>Logout client-side: il server non mantiene blacklist; il client scarta il
 * token. L'identità in {@code /me} viene dal principal JWT, mai dal client.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
		return authenticationService.register(request);
	}

	@PostMapping("/login")
	public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
		return authenticationService.login(request);
	}

	@GetMapping("/me")
	public CurrentUserResponse me(Authentication authentication) {
		return authenticationService.getCurrentUser(authentication.getName());
	}

	@PostMapping("/logout")
	public Map<String, String> logout() {
		return Map.of("message", "Logout effettuato. Scarta il token JWT lato client.");
	}
}
