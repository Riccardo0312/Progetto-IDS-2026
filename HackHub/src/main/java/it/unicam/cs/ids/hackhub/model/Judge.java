package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "judges")
@Getter
@Setter
@NoArgsConstructor
public class Judge extends StaffMember {

	@OneToMany(mappedBy = "judge")
	private List<Hackathon> assignedHackathons = new ArrayList<>();

	@OneToMany(mappedBy = "judge")
	private List<Evaluation> evaluations = new ArrayList<>();

}
