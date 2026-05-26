package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String name;

	// creator: User RIMOSSO — il leader è derivato da members con role == LEADER.

	@OneToMany(mappedBy = "team")
	private List<TeamMember> members = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<Invitation> invitations = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<HackathonRegistration> registrations = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<SupportRequest> supportRequests = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<ViolationReport> violationReports = new ArrayList<>();

	// ------ Domain helpers (leader come ruolo) ------

	/** Ritorna il leader, se presente. */
	public Optional<TeamMember> findLeader() {
		return members.stream().filter(TeamMember::isLeader).findFirst();
	}

	/**
	 * Ritorna il leader; lancia {@link IllegalStateException} se assente
	 * (invariante di dominio violata).
	 */
	public TeamMember getLeader() {
		return findLeader().orElseThrow(
				() -> new IllegalStateException(
						"Team " + id + " senza leader: invariante di dominio violata"));
	}

	/** {@code true} se esiste esattamente un leader. */
	public boolean hasLeader() {
		return members.stream().filter(TeamMember::isLeader).count() == 1L;
	}

	/** {@code true} se l'utente con id {@code userId} è il leader del team. */
	public boolean isLedBy(Long userId) {
		return findLeader()
				.map(l -> l.getUser() != null && userId != null
						&& userId.equals(l.getUser().getId()))
				.orElse(false);
	}

	/**
	 * Trasferisce la leadership al membro target in modo atomico.
	 * Mantiene l'invariante "esattamente un leader".
	 * Il membro target deve già appartenere al team.
	 */
	public void promoteToLeader(TeamMember target) {
		if (target == null || !members.contains(target)) {
			throw new IllegalArgumentException("Il membro non appartiene a questo team");
		}
		members.stream().filter(TeamMember::isLeader).forEach(TeamMember::revokeLeaderRole);
		target.assignLeaderRole();
	}
}
