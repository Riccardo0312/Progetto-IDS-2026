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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evaluations")
@Getter
@Setter
@NoArgsConstructor
public class Evaluation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false, length = 2000)
	private String judgment;

	@Min(0)
	@Max(10)
	@Column(nullable = false)
	private int score;

	@NotNull
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "submission_id", nullable = false, unique = true)
	private Submission submission;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "judge_id", nullable = false)
	private Judge judge;

}
