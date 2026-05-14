package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "mentoring_call_proposals")
@Getter
@Setter
@NoArgsConstructor
public class MentoringCallProposal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime proposedAt;

	@NotBlank
	@URL
	@Size(max = 500)
	@Column(nullable = false, length = 500)
	private String bookingLink;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mentor_id", nullable = false)
	private Mentor mentor;

	@NotNull
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "support_request_id", nullable = false, unique = true)
	private SupportRequest supportRequest;

}
