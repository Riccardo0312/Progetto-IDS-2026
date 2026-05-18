package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import java.util.List;
import it.unicam.cs.ids.hackhub.service.interfaces.GuestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GuestServiceImpl implements GuestService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;

    public GuestServiceImpl(HackathonRepository hackathonRepository,
                            UserRepository userRepository) {

        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public List<Hackathon> getAllHackathons() {
        List<Hackathon> hackathons = hackathonRepository.findAll();
        for (int i = 0; i < hackathons.size(); i++) {
            hackathons.get(i).updateStatus();
        }
        hackathonRepository.saveAll(hackathons);
        return hackathons;
    }

    @Override
    public List<Hackathon> getHackathonsByStatus(HackathonStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Lo stato non può essere null");
        }
        return hackathonRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Hackathon getHackathonById(Long hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("L'ID non può essere null");
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Hackathon non trovato con ID: " + hackathonId));
        hackathon.updateStatus();
        hackathonRepository.save(hackathon);
        return hackathon;
    }

    @Transactional
    @Override
    public User register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("L'utente non può essere null");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Email già registrata: " + user.getEmail());
        }
        return userRepository.save(user);
    }


}