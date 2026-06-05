package it.unicam.cs.ids.hackhub.config;

import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean di autenticazione: {@link UserDetailsService}, {@link PasswordEncoder}
 * (BCrypt), {@link AuthenticationProvider} (DAO) e {@link AuthenticationManager}.
 *
 * <p>L'identità Spring Security è l'email: {@code userDetailsService} carica lo
 * {@link it.unicam.cs.ids.hackhub.model.User} via {@code UserRepository.findByEmail}.
 * {@code User} implementa {@code UserDetails}, quindi non serve adapter.
 */
@Configuration
public class ApplicationConfig {

	private final UserRepository userRepository;

	public ApplicationConfig(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return email -> userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
			throws Exception {
		return config.getAuthenticationManager();
	}
}
