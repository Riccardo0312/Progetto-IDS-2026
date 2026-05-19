package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.User;
import java.util.List;

public interface IGuestService {

    List<Hackathon> getAllHackathons();

    List<Hackathon> getHackathonsByStatus(HackathonStatus status);

    Hackathon getHackathonById(Long hackathonId);

    User register(User user);
}
