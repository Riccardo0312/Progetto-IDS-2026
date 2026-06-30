package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonDetailDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonListItemDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonRegistrationsDTO;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.User;
import java.util.List;

public interface IGuestService {

    List<HackathonListItemDTO> getAllHackathons();

    List<HackathonListItemDTO> getHackathonsByStatus(HackathonStatus status);

    HackathonDetailDTO getHackathonById(Long hackathonId);

    User register(User user);

    HackathonRegistrationsDTO getHackathonRegistrations(Long hackathonId);
}
