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
import jakarta.persistence.OneToOne;
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
import java.util.Objects;
import it.unicam.cs.ids.hackhub.model.state.hackathon.HackathonState;
import it.unicam.cs.ids.hackhub.model.state.hackathon.HackathonStateFactory;
import lombok.AccessLevel;
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
	@Setter(AccessLevel.PROTECTED)
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

	@OneToOne(mappedBy = "hackathon", fetch = FetchType.LAZY)
	private PrizeDisbursement prizeDisbursement;

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
		for (HackathonRegistration registration : registrations) {
			if (registration.getSubmission() == null ||
					registration.getSubmission().getEvaluation() == null) {
				return false;
			}
		}
		return true;
	}

	public void updateStatus() {
		updateStatus(LocalDate.now());
	}

	public void updateStatus(LocalDate currentDate) {
		this.status = getCurrentState().updateStatus(
				currentDate, registrationDeadline, startDate, endDate);
	}

	public void ensureMentorActionsAllowed() {
		getCurrentState().ensureMentorActionsAllowed(id);
	}

	public void ensureSupportRequestsAllowed() {
		getCurrentState().ensureSupportRequestsAllowed(id);
	}

	public void ensureSubmissionActionsAllowed() {
		getCurrentState().ensureSubmissionActionsAllowed(id);
	}

	public void ensureJudgingActionsAllowed() {
		getCurrentState().ensureJudgingActionsAllowed(id);
	}

	public void ensureWinnerProclamationAllowed() {
		getCurrentState().ensureWinnerProclamationAllowed(id);
	}

	public void ensureCancellationAllowed() {
		getCurrentState().ensureCancellationAllowed(id);
	}

	/**
	 * Verifica che siano accettabili nuove iscrizioni di team. Vedi ADR 0003.
	 *
	 * <p>Permesso solo finche lo stato e {@code REGISTRATION} <strong>e</strong>
	 * la data corrente non e oltre {@code registrationDeadline}. Il check
	 * temporale e necessario perche tra {@code registrationDeadline} e
	 * {@code startDate} l'hackathon e ancora in {@code REGISTRATION} (sotto-fase
	 * "iscrizioni chiuse, evento non iniziato"), ma le iscrizioni vanno rifiutate.
	 */
	public void ensureNewRegistrationsAllowed(LocalDate currentDate) {
		if (currentDate == null) {
			throw new IllegalArgumentException("La data corrente non può essere null");
		}
		if (status != HackathonStatus.REGISTRATION) {
			throw new it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException(
					id, status, HackathonStatus.REGISTRATION);
		}
		if (registrationDeadline != null && currentDate.isAfter(registrationDeadline)) {
			throw new IllegalStateException(
					"Le iscrizioni per l'hackathon " + id
							+ " sono chiuse dal " + registrationDeadline);
		}
	}

	/**
	 * Verifica che la modifica dei parametri sia consentita. Vedi ADR 0003.
	 *
	 * <p>Permesso solo se lo stato e {@code REGISTRATION} <strong>e</strong> la
	 * data corrente non e oltre {@code registrationDeadline}: una volta chiuse
	 * le iscrizioni, i team registrati hanno fatto affidamento sui parametri
	 * dichiarati, quindi cambiarli sarebbe scorretto verso di loro.
	 */
	public void ensureModificationAllowed(LocalDate currentDate) {
		if (currentDate == null) {
			throw new IllegalArgumentException("La data corrente non può essere null");
		}
		if (status != HackathonStatus.REGISTRATION) {
			throw new it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException(
					id, status, HackathonStatus.REGISTRATION);
		}
		if (registrationDeadline != null && currentDate.isAfter(registrationDeadline)) {
			throw new IllegalStateException(
					"Modifica non permessa dopo la scadenza iscrizioni dell'hackathon " + id);
		}
	}

	/**
	 * Transizione esplicita verso lo stato {@code CONCLUDED}.
	 */
	public void concludeWith(Team winningTeam) {
		if (winningTeam == null) {
			throw new IllegalArgumentException("Il team vincitore non può essere null");
		}
		ensureWinnerProclamationAllowed();
		if (!hasRegisteredTeam(winningTeam)) {
			throw new IllegalArgumentException("Il team vincitore non è registrato all'hackathon");
		}
		if (!allEvaluated()) {
			throw new IllegalStateException("Ci sono ancora sottomissioni non valutate");
		}
		this.winningTeam = winningTeam;
		this.status = HackathonStatus.CONCLUDED;
	}

	/**
	 * Transizione esplicita verso lo stato {@code CANCELLED}. Vedi ADR 0001.
	 *
	 * <p>Permesso solo da {@code REGISTRATION} o {@code READY}; le registrazioni
	 * dei team già iscritti vengono preservate, l'hackathon resta visibile come
	 * "annullato" per audit.
	 */
	public void cancel() {
		ensureCancellationAllowed();
		this.status = HackathonStatus.CANCELLED;
	}

	/**
	 * Modifica i parametri descrittivi e logistici dell'hackathon. Vedi ADR 0001
	 * e CONTEXT "Modifica dell'hackathon".
	 *
	 * <p>Permesso solo da {@code REGISTRATION}. Valida gli invarianti di dominio
	 * e rifiuta la modifica se le nuove date farebbero transitare lo stato in
	 * {@code READY}/{@code RUNNING}/{@code EVALUATION} immediatamente dopo
	 * l'applicazione (defense-in-depth).
	 *
	 * @param currentDate data di riferimento per il controllo di transizione
	 */
	public void update(
			String name, String rules, String location, BigDecimal prizeMoney,
			int maxTeamSize, LocalDate registrationDeadline, LocalDate startDate,
			LocalDate endDate, LocalDate currentDate) {
		ensureModificationAllowed(currentDate);
		validateUpdateInputs(prizeMoney, maxTeamSize,
				registrationDeadline, startDate, endDate, currentDate);
		ensureMaxTeamSizeAccommodatesExistingTeams(maxTeamSize);

		this.name = name;
		this.rules = rules;
		this.location = location;
		this.prizeMoney = prizeMoney;
		this.maxTeamSize = maxTeamSize;
		this.registrationDeadline = registrationDeadline;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	private void validateUpdateInputs(
			BigDecimal prizeMoney, int maxTeamSize,
			LocalDate registrationDeadline, LocalDate startDate,
			LocalDate endDate, LocalDate currentDate) {
		if (registrationDeadline == null || startDate == null
				|| endDate == null || currentDate == null) {
			throw new IllegalArgumentException("Le date non possono essere null");
		}
		if (prizeMoney == null || prizeMoney.signum() < 0) {
			throw new IllegalArgumentException("Il premio in denaro non può essere negativo");
		}
		if (maxTeamSize <= 0) {
			throw new IllegalArgumentException("La dimensione massima del team deve essere positiva");
		}
		if (registrationDeadline.isBefore(currentDate)) {
			throw new IllegalArgumentException(
					"La scadenza iscrizioni non può essere nel passato");
		}
		if (startDate.isBefore(registrationDeadline)) {
			throw new IllegalArgumentException(
					"La data di inizio deve essere successiva alla scadenza iscrizioni");
		}
		if (endDate.isBefore(startDate)) {
			throw new IllegalArgumentException(
					"La data di fine deve essere successiva alla data di inizio");
		}
	}

	private void ensureMaxTeamSizeAccommodatesExistingTeams(int newMaxTeamSize) {
		if (newMaxTeamSize >= this.maxTeamSize) {
			return; // aumento o invariato: nessun rischio.
		}
		for (HackathonRegistration registration : registrations) {
			Team team = registration.getTeam();
			if (team == null) {
				continue;
			}
			int size = team.getMembers() == null ? 0 : team.getMembers().size();
			if (size > newMaxTeamSize) {
				throw new IllegalStateException(
						"Il team " + team.getId() + " ha " + size
								+ " membri, supera la nuova dimensione massima "
								+ newMaxTeamSize);
			}
		}
	}

	private boolean hasRegisteredTeam(Team team) {
		for (HackathonRegistration registration : registrations) {
			Team registeredTeam = registration.getTeam();
			if (registeredTeam == team) {
				return true;
			}
			if (registeredTeam != null
					&& registeredTeam.getId() != null
					&& Objects.equals(registeredTeam.getId(), team.getId())) {
				return true;
			}
		}
		return false;
	}

	private HackathonState getCurrentState() {
		return HackathonStateFactory.fromStatus(status);
	}


}
