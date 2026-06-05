package it.unicam.cs.ids.hackhub.service.factory;

import it.unicam.cs.ids.hackhub.model.Judge;
import it.unicam.cs.ids.hackhub.model.Mentor;
import it.unicam.cs.ids.hackhub.model.Organizer;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.UserRole;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea e persiste l'utente corretto in base al ruolo globale.
 *
 * <p>Equivalente di {@code SpringReadyUtenteFactory}: unico punto che cifra la
 * password (BCrypt) e istanzia la sottoclasse JPA giusta
 * ({@code ORGANIZER->Organizer}, {@code MENTOR->Mentor}, {@code JUDGE->Judge},
 * {@code USER/ADMIN->User}). Valida l'unicità dell'email.
 */
@Component
public class HackHubUserFactory {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public HackHubUserFactory(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * @param rawPassword password in chiaro; viene cifrata qui con BCrypt
	 * @return utente salvato (sottoclasse coerente con {@code role})
	 * @throws IllegalArgumentException email già registrata o ruolo nullo
	 */
	public User create(String name, String email, String rawPassword, UserRole role) {
		if (role == null) {
			throw new IllegalArgumentException("Il ruolo è obbligatorio");
		}
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Email già registrata: " + email);
		}

		User user = instantiate(role);
		user.setName(name);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setRole(role);
		user.setEnabled(true);

		return userRepository.save(user);
	}

	private User instantiate(UserRole role) {
		return switch (role) {
			case ORGANIZER -> new Organizer();
			case MENTOR -> new Mentor();
			case JUDGE -> new Judge();
			case USER, ADMIN -> new User();
		};
	}
}
