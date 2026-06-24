package it.unicam.cs.ids.hackhub.security;

import it.unicam.cs.ids.hackhub.dto.auth.AuthenticationResponse;
import it.unicam.cs.ids.hackhub.dto.auth.CurrentUserResponse;
import it.unicam.cs.ids.hackhub.dto.auth.LoginRequest;
import it.unicam.cs.ids.hackhub.dto.auth.RegisterRequest;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import it.unicam.cs.ids.hackhub.service.factory.HackHubUserFactory;
import it.unicam.cs.ids.hackhub.service.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Orchestratore di registrazione e login.
 *
 * <p>{@code register} delega la creazione/cifratura alla {@link HackHubUserFactory}
 * e genera il JWT. {@code login} autentica via {@link AuthenticationManager}
 * (BCrypt + enabled check) e genera il JWT. La password non transita mai nelle
 * risposte.
 */
@Service
public class AuthenticationService {

	private final HackHubUserFactory userFactory;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserMapper userMapper;
	private final TokenRevocationService tokenRevocationService;

	public AuthenticationService(
			HackHubUserFactory userFactory,
			UserRepository userRepository,
			JwtService jwtService,
			AuthenticationManager authenticationManager,
			UserMapper userMapper,
			TokenRevocationService tokenRevocationService) {
		this.userFactory = userFactory;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.userMapper = userMapper;
		this.tokenRevocationService = tokenRevocationService;
	}

	@Transactional
	public AuthenticationResponse register(RegisterRequest request) {
		User user = userFactory.create(
				request.name(), request.email(), request.password(), request.role());
		String token = jwtService.generateToken(user);
		return userMapper.toAuthenticationResponse(user, token);
	}

	@Transactional(readOnly = true)
	public AuthenticationResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(
						request.email(), request.password()));

		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new UsernameNotFoundException(
						"Utente non trovato: " + request.email()));
		String token = jwtService.generateToken(user);
		return userMapper.toAuthenticationResponse(user, token);
	}

	@Transactional(readOnly = true)
	public CurrentUserResponse getCurrentUser(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException(
						"Utente non trovato: " + email));
		return userMapper.toCurrentUser(user);
	}

	@Transactional
	public void logout(String authorizationHeader) {
		if (!StringUtils.hasText(authorizationHeader)
				|| !authorizationHeader.startsWith("Bearer ")) return;
		tokenRevocationService.revoke(authorizationHeader.substring(7));
	}
}
