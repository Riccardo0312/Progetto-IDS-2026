package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "users",
		uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String name;

	@NotBlank
	@Email
	@Size(max = 150)
	@Column(nullable = false, length = 150)
	private String email;

	@NotBlank
	@Size(min = 8, max = 255)
	@Column(nullable = false)
	private String password;

}
