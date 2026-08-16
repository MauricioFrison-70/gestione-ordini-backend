package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.repository.AgenteRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.agente.AgenteMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AgenteRepositoryImpl implements AgenteRepository {

    private final AgenteJpaRepository jpa;
    private final AgenteMapper mapper;

    public AgenteRepositoryImpl(AgenteJpaRepository jpa, AgenteMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<Agente> findAll() {
        return jpa.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Agente> findById(Long id) {
        return jpa.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Agente save(Agente agente) {
        AgenteEntity entity = mapper.toEntity(agente);
        AgenteEntity salvato = jpa.save(entity);
        return mapper.toDomain(salvato);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }
}
