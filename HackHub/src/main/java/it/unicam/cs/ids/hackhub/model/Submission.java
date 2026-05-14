package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 150)
	@Column(nullable = false, length = 150)
	private String title;

	@NotBlank
	@Column(nullable = false, length = 4000)
	private String description;

	@NotBlank
	@URL
	@Size(max = 500)
	@Column(nullable = false, length = 500)
	private String projectLink;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime uploadedAt;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime lastModifiedAt;

	@NotNull
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "registration_id", nullable = false, unique = true)
	private HackathonRegistration registration;

	@OneToOne(mappedBy = "submission")
	private Evaluation evaluation;

}
