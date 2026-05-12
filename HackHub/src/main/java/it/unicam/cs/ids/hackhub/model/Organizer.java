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
@Table(name = "organizers")
@Getter
@Setter
@NoArgsConstructor
public class Organizer extends StaffMember {

	@OneToMany(mappedBy = "organizer")
	private List<Hackathon> organizedHackathons = new ArrayList<>();

}
