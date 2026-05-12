package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_members")
@Getter
@Setter
@NoArgsConstructor
public class TeamMember extends User {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

}
