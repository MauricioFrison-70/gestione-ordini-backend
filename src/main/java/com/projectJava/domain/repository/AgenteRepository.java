package com.seuprojeto.domain.repository;

import com.seuprojeto.domain.model.Agente;
import java.util.List;
import java.util.Optional;

public interface AgenteRepository {

    Agente salva(Agente agente);

    Optional<Agente> trovaPerId(Long id);

    List<Agente> trovaTutti();

    void elimina(Long id);
}

