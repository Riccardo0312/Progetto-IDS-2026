package it.unicam.cs.ids.hackhub.model;

import it.unicam.cs.ids.hackhub.model.state.InvitationState;
import it.unicam.cs.ids.hackhub.model.state.InvitationStateFactory;
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
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
public class Invitation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	private InvitationStatus status = InvitationStatus.PENDING;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipient_id", nullable = false)
	private User recipient; //rappresenta l' utente destinatario dell' invito

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	public InvitationState getCurrentState() {
		return InvitationStateFactory.fromStatus(status);
	}

	public void accept() {
		getCurrentState().ensureCanAccept(id);
		this.status = InvitationStatus.ACCEPTED;
	}

	public void reject() {
		getCurrentState().ensureCanReject(id);
		this.status = InvitationStatus.REJECTED;
	}
}
