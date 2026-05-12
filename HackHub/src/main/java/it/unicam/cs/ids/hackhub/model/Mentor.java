package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mentors")
@Getter
@Setter
@NoArgsConstructor
public class Mentor extends StaffMember {

	@ManyToMany(mappedBy = "mentors")
	private List<Hackathon> supportedHackathons = new ArrayList<>();

	@OneToMany(mappedBy = "mentor")
	private List<MentoringCallProposal> callProposals = new ArrayList<>();

	@OneToMany(mappedBy = "mentor")
	private List<ViolationReport> violationReports = new ArrayList<>();

}
