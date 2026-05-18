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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hackathons")
@Getter
@Setter
@NoArgsConstructor
public class Hackathon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 150)
	@Column(nullable = false, length = 150)
	private String name;

	@NotBlank
	@Column(nullable = false, length = 4000)
	private String rules;

	@NotNull
	@FutureOrPresent
	@Column(nullable = false)
	private LocalDate registrationDeadline;

	@NotNull
	@Column(nullable = false)
	private LocalDate startDate;

	@NotNull
	@Column(nullable = false)
	private LocalDate endDate;

	@NotBlank
	@Size(max = 200)
	@Column(nullable = false, length = 200)
	private String location;

	@NotNull
	@Min(0)
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal prizeMoney = BigDecimal.ZERO;

	@Positive
	@Column(nullable = false)
	private int maxTeamSize;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private HackathonStatus status = HackathonStatus.REGISTRATION;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organizer_id", nullable = false)
	private Organizer organizer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "judge_id")
	private Judge judge;

	@ManyToMany
	@JoinTable(
			name = "hackathon_mentors",
			joinColumns = @JoinColumn(name = "hackathon_id"),
			inverseJoinColumns = @JoinColumn(name = "mentor_id"))
	private List<Mentor> mentors = new ArrayList<>();

	@OneToMany(mappedBy = "hackathon")
	private List<HackathonRegistration> registrations = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winning_team_id")
	private Team winningTeam;

	@OneToMany(mappedBy = "hackathon")
	private List<ViolationReport> violationReports = new ArrayList<>();

	public Hackathon(String name, String rules, String location,BigDecimal prizeMoney, int maxTeamSize, LocalDate registrationDeadline,LocalDate startDate , LocalDate endDate ) {

		this.name = name;
		this.rules=rules;
		this.location=location;
		this.prizeMoney=prizeMoney;
		this.maxTeamSize=maxTeamSize;
		this.registrationDeadline=registrationDeadline;
		this.startDate=startDate;
		this.endDate=endDate;

	}

	public void addMentor(Mentor mentor){

		if (mentor == null || mentors.contains(mentor)) {
			throw new IllegalArgumentException("Mentore non valido o già presente");
		}
		mentors.add(mentor);
	}

	public void addJudge(Judge judge){
		if (judge == null ) {
			throw new IllegalArgumentException("Judge non valido");
		}
		this.judge = judge;
	}

	public void removeMentor(Mentor mentor) {
		if (mentor == null || !mentors.contains(mentor)) {
			throw new IllegalArgumentException("Mentore non valido o non presente");
		}
		mentors.remove(mentor);
	}

	public void removeJudge() {
		if (this.judge == null) {
			throw new IllegalArgumentException("Nessun giudice trovato");
		}
		this.judge = null;
	}

	public boolean allEvaluated() {
		if (registrations.isEmpty()) {
			return false;
		}
		for (int i = 0; i < registrations.size(); i++) {
			if (registrations.get(i).getSubmission() == null ||
					registrations.get(i).getSubmission().getEvaluation() == null) {
				return false;
			}
		}
		return true;
	}

	public void updateStatus() {
		this.status = HackathonStatus.updateStatus(status, registrationDeadline, endDate);
	}



}
