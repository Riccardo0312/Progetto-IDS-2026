package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.auth.AuthenticationResponse;
import it.unicam.cs.ids.hackhub.dto.auth.CurrentUserResponse;
import it.unicam.cs.ids.hackhub.model.User;
import java.util.List;
import org.springframework.stereotype.Component;

/** Mappa {@link User} sui DTO auth, escludendo sempre la password. */
@Component
public class UserMapper {

	public AuthenticationResponse toAuthenticationResponse(User user, String token) {
		return new AuthenticationResponse(
				token,
				"Bearer",
				user.getId(),
				user.getEmail(),
				user.getName(),
				roles(user));
	}

	public CurrentUserResponse toCurrentUser(User user) {
		return new CurrentUserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				roles(user));
	}

	/** Ruoli senza prefisso {@code ROLE_}; lista vuota se ruolo assente. */
	private List<String> roles(User user) {
		return user.getRole() == null ? List.of() : List.of(user.getRole().name());
	}
}
