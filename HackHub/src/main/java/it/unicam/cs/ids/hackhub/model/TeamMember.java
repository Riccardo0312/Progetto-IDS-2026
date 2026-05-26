package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "team_members",
		uniqueConstraints = @UniqueConstraint(name = "uk_team_members_user", columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class TeamMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	/** Ruolo nel team. Default {@code MEMBER}. */
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private TeamRole role = TeamRole.MEMBER;

	/** Costruttore di dominio: associa user, team e ruolo in modo esplicito. */
	public TeamMember(User user, Team team, TeamRole role) {
		this.user = user;
		this.team = team;
		this.role = role;
	}

	/** {@code true} se il membro è il leader del team. */
	public boolean isLeader() {
		return role == TeamRole.LEADER;
	}

	/** {@code true} se il membro è un membro standard. */
	public boolean isMember() {
		return role == TeamRole.MEMBER;
	}

	/** Promuove a leader. Usare solo via {@link Team#promoteToLeader}. */
	void assignLeaderRole() {
		this.role = TeamRole.LEADER;
	}

	/** Retrocede a membro standard. Usare solo via {@link Team#promoteToLeader}. */
	void revokeLeaderRole() {
		this.role = TeamRole.MEMBER;
	}
}
