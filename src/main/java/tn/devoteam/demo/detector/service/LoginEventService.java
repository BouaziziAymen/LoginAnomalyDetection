package tn.devoteam.demo.detector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.devoteam.demo.detector.data.LoginEventMapper;
import tn.devoteam.demo.detector.data.LoginEventWriteRepository;
import tn.devoteam.demo.detector.model.LoginEvent;

/**
 * Service métier pour les événements de connexion.
 */
@RequiredArgsConstructor
@Service
public class LoginEventService {

    private final LoginEventWriteRepository writeRepository;
    private final LoginEventMapper mapper;

    public void persist(LoginEvent event) {
        writeRepository.save(mapper.toDocument(event));
    }
}

