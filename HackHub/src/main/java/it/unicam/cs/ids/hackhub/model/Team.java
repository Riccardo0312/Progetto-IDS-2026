package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String name;

	@OneToMany(mappedBy = "team")
	private List<TeamMember> members = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<Invitation> invitations = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<HackathonRegistration> registrations = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<SupportRequest> supportRequests = new ArrayList<>();

	@OneToMany(mappedBy = "team")
	private List<ViolationReport> violationReports = new ArrayList<>();

    public void setCreator(User creator) {
		this.members.add(new TeamMember());
		this.members.get(0).setUser(creator);
    }
}
