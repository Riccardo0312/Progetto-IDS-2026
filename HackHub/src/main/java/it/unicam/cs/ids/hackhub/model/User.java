package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(
		name = "users",
		uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String name;

	@NotBlank
	@Email
	@Size(max = 150)
	@Column(nullable = false, length = 150)
	private String email;

	@NotBlank
	@Size(min = 8, max = 255)
	@Column(nullable = false)
	private String password;

	/** Ruolo globale di piattaforma. Mappato su authority {@code ROLE_<role>}. */
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private UserRole role;

	/** Account abilitato. Mappa {@link UserDetails#isEnabled()}. */
	@Column(nullable = false)
	private boolean enabled = true;

	// --- UserDetails ---

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (role == null) {
			return List.of();
		}
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	/** Username Spring Security = email. */
	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}
