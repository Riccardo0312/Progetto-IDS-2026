package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "violation_reports")
@Getter
@Setter
@NoArgsConstructor
public class ViolationReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false, length = 2000)
	private String description;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime reportedAt;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mentor_id", nullable = false)
	private Mentor mentor;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hackathon_id", nullable = false)
	private Hackathon hackathon;

}
